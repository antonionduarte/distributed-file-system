package tp1.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.ClientFactory;
import tp1.server.operations.*;
import util.Discovery;
import util.Pair;
import util.Secret;
import util.Token;
import util.kafka.KafkaPublisher;
import util.kafka.KafkaSubscriber;
import util.kafka.RecordProcessor;
import util.kafka.sync.SyncPoint;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class JavaReplicatedDirectory extends Thread implements Directory, RecordProcessor {

	// String: filename
	private final Map<String, FileInfo> files;

	// String: fileId
	private final Map<String, Set<URI>> URIsPerFile;

	// String: userId
	private final Map<String, Set<FileInfo>> accessibleFilesPerUser;

	private final ClientFactory clientFactory;

	private static final String FROM_BEGINNING = "earliest";
	private static final String DIRECTORY_REPLICATION_TOPIC = "directory_replication";
	private static final String DELETE_USER_TOPIC = "delete_user";
	private static final String KAFKA_BROKERS = "kafka:9092";

	//final String replicaId;
	final KafkaPublisher sender;
	final KafkaSubscriber receiver;

	private final SyncPoint<String> syncPoint;

	private final Gson gson;

	public JavaReplicatedDirectory(SyncPoint<String> syncPoint) {
		this.sender = KafkaPublisher.createPublisher(KAFKA_BROKERS);
		this.receiver = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(DIRECTORY_REPLICATION_TOPIC, DELETE_USER_TOPIC), FROM_BEGINNING);
		this.receiver.start(false, this);
		this.syncPoint = syncPoint;
		this.gson = new GsonBuilder().create();
		this.files = new ConcurrentHashMap<>();
		this.accessibleFilesPerUser = new ConcurrentHashMap<>();
		this.clientFactory = ClientFactory.getInstance();
		this.URIsPerFile = new ConcurrentHashMap<>();
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
		String fileId = String.format("%s_%s", userId, filename);

		Set<URI> serverURIs = new HashSet<>();
		FileInfo file;
		synchronized (this) {
			file = files.get(fileId);

			if (file != null && !file.getOwner().equals(userId)) {
				return Result.error(Result.ErrorCode.FORBIDDEN);
			}

			Pair<URI, Users> usersUriAndClient = clientFactory.getUsersClient();
			Users usersClient = usersUriAndClient.second();
			var userResult = usersClient.getUser(userId, password);

			if (userResult == null) {
				return Result.error(Result.ErrorCode.INTERNAL_ERROR);
			}

			// authenticate the user
			if (!userResult.isOK()) {
				return Result.error(userResult.error());
			}

			Set<Pair<URI, Files>> filesUrisAndClients = new HashSet<>();

			if (file == null) {
				filesUrisAndClients = clientFactory.getFilesClients();
			} else {
				for (URI uri : intersectionWithDiscoveryOfFiles(file)) {
					var client = clientFactory.getFilesClient(uri);
					if (client != null) {
						filesUrisAndClients.add(client);
					}
				}
			}

			Result<Void> filesResult = null;
			for (Pair<URI, Files> filesUriAndClient : filesUrisAndClients) {
				serverURIs.add(filesUriAndClient.first());
				Files filesClient = filesUriAndClient.second();

				filesResult = filesClient.writeFile(fileId, data, Token.generate(Secret.get(), fileId));
				if (!filesResult.isOK()) {
					return Result.error(filesResult.error());
				}
			}

			if (filesResult == null) {
				return Result.error(Result.ErrorCode.INTERNAL_ERROR);
			}

			if (file == null) {
				Set<URI> fileURIs = ConcurrentHashMap.newKeySet();
				for (URI serverURI : serverURIs)
					fileURIs.add(URI.create(String.format("%s%s/%s", serverURI, RestFiles.PATH, fileId)));
				file = new FileInfo(userId, filename, fileURIs.toArray()[0].toString(), ConcurrentHashMap.newKeySet());
				URIsPerFile.put(fileId, fileURIs);
			}

			files.put(fileId, file);
		}

		var listFiles = accessibleFilesPerUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
		listFiles.add(file);

		var writeFile = new WriteFile(filename, data, userId, password, serverURIs, file);
		var version = sender.publish(DIRECTORY_REPLICATION_TOPIC, OperationType.WRITE_FILE.name(), gson.toJson(writeFile));
		this.syncPoint.waitForResult(version);

		file = files.get(fileId);
		return Result.ok(file);
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) {
		String fileId = String.format("%s_%s", userId, filename);

		FileInfo file;
		synchronized (this) {
			file = files.get(fileId);

			if (file != null) {
				if (!file.getOwner().equals(userId)) {
					return Result.error(Result.ErrorCode.FORBIDDEN);
				}
			} else {
				return Result.error(Result.ErrorCode.NOT_FOUND);
			}

			Users usersClient = clientFactory.getUsersClient().second();
			var userResult = usersClient.getUser(userId, password);

			if (userResult == null) {
				return Result.error(Result.ErrorCode.INTERNAL_ERROR);
			}

			// authenticate the user
			if (!userResult.isOK()) {
				return Result.error(userResult.error());
			}

			Result<Void> filesResult = null;
			for (URI fileURI : intersectionWithDiscoveryOfFiles(file)) {
				Files filesClient = clientFactory.getFilesClient(fileURI).second();
				filesResult = filesClient.deleteFile(fileId, Token.generate(Secret.get(), fileId));
				clientFactory.deletedFileFromServer(fileURI);
			}

			if (filesResult == null) {
				return Result.error(Result.ErrorCode.INTERNAL_ERROR);
			}

			if (!filesResult.isOK()) {
				return Result.error(filesResult.error());
			}
		}

		var deleteFile = new DeleteFile(filename, userId, password, files.get(fileId));
		var version = sender.publish(DIRECTORY_REPLICATION_TOPIC, OperationType.DELETE_FILE.name(), gson.toJson(deleteFile));
		this.syncPoint.waitForResult(version);

		return Result.ok();
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
		String fileId = String.format("%s_%s", userId, filename);

		FileInfo file = files.get(fileId);

		if (file == null) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		Users usersClient = clientFactory.getUsersClient().second();
		var userResult = usersClient.getUser(userId, password);
		var userShareResult = usersClient.getUser(userIdShare, "");

		if (userResult == null || userShareResult == null) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

		// check if userid exists
		if (userResult.error() == Result.ErrorCode.NOT_FOUND) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		if (userShareResult.error() == Result.ErrorCode.NOT_FOUND) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		if (userResult.error() == Result.ErrorCode.FORBIDDEN) {
			return Result.error(Result.ErrorCode.FORBIDDEN);
		}

		var shareFile = new ShareFile(filename, userId, userIdShare, password);
		var version = sender.publish(DIRECTORY_REPLICATION_TOPIC, OperationType.SHARE_FILE.name(), gson.toJson(shareFile));
		this.syncPoint.waitForResult(version);

		return Result.ok();
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
		String fileId = String.format("%s_%s", userId, filename);

		FileInfo file = files.get(fileId);

		if (file == null) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		Users usersClient = clientFactory.getUsersClient().second();
		var userResult = usersClient.getUser(userId, password);
		var userShareResult = usersClient.getUser(userIdShare, "");

		if (userResult == null || userShareResult == null) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

		// check if userid exists
		if (userResult.error() == Result.ErrorCode.NOT_FOUND) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		if (userShareResult.error() == Result.ErrorCode.NOT_FOUND) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		if (userResult.error() == Result.ErrorCode.FORBIDDEN) {
			return Result.error(Result.ErrorCode.FORBIDDEN);
		}

		var unshareFile = new UnshareFile(filename, userId, userIdShare, password);
		var version = sender.publish(DIRECTORY_REPLICATION_TOPIC, OperationType.UNSHARE_FILE.name(), gson.toJson(unshareFile));
		this.syncPoint.waitForResult(version);

		return Result.ok();
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
		String fileId = String.format("%s_%s", userId, filename);

		FileInfo file = files.get(fileId);

		if (file == null) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		Users usersClient = clientFactory.getUsersClient().second();
		var userResult = usersClient.getUser(userId, "");

		if (userResult == null) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

		// check if userid exists
		if (userResult.error() == Result.ErrorCode.NOT_FOUND) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		var accUserResult = usersClient.getUser(accUserId, password);

		if (accUserResult == null) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

		// authenticate the user
		if (!accUserResult.isOK()) {
			return Result.error(accUserResult.error());
		}
		// file not shared with user
		if (!file.getSharedWith().contains(accUserId) && !file.getOwner().equals(accUserId)) {
			return Result.error(Result.ErrorCode.FORBIDDEN);
		}

		intersectionWithDiscoveryOfFiles(file);

		var getFile = new GetFile(filename, userId, accUserId, password, files.get(fileId));
		var version = sender.publish(DIRECTORY_REPLICATION_TOPIC, OperationType.GET_FILE.name(), gson.toJson(getFile));
		this.syncPoint.waitForResult(version);

		file = files.get(fileId);
		return Result.ok(URI.create(file.getFileURL()));
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password) {
		Users usersClient;
		usersClient = clientFactory.getUsersClient().second();
		var userResult = usersClient.getUser(userId, password);

		if (userResult == null) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

		// authenticate userId
		if (!userResult.isOK()) {
			return Result.error(userResult.error());
		}

		var list = accessibleFilesPerUser.get(userId);
		if (list == null) {
			return Result.ok(new CopyOnWriteArrayList<>());
		} else {
			return Result.ok(new CopyOnWriteArrayList<>(list));
		}
	}

	private void removeUserInfo(String userId) {
		var listFiles = accessibleFilesPerUser.remove(userId);
		if (listFiles == null) {
			return;
		}

		for (FileInfo file : listFiles) {
			if (file.getOwner().equals(userId)) {
				// delete user's files from others accessible files
				for (String user : file.getSharedWith()) {
					if (!user.equals(userId)) {
						accessibleFilesPerUser.get(user).remove(file);
					}
				}
			}
			// delete user from shareWith of others files
			file.getSharedWith().remove(userId);
		}
	}

	@Override
	public void onReceive(ConsumerRecord<String, String> record) {
		if (record.topic().equals(DIRECTORY_REPLICATION_TOPIC)) {
			var version = record.offset();
			var result = record.value();

			switch (OperationType.valueOf(record.key())) {
				case WRITE_FILE -> dir_writeFile(gson.fromJson(result, WriteFile.class));
				case GET_FILE -> dir_getFile(gson.fromJson(result, GetFile.class));
				case SHARE_FILE -> dir_shareFile(gson.fromJson(result, ShareFile.class));
				case DELETE_FILE -> dir_deleteFile(gson.fromJson(result, DeleteFile.class));
				case UNSHARE_FILE -> dir_unshareFile(gson.fromJson(result, UnshareFile.class));
			}

			this.syncPoint.setResult(version, result);
		} else if (record.topic().equals(DELETE_USER_TOPIC)) {
			removeUserInfo(record.value());
		}
	}

	private void dir_getFile(GetFile getFile) {
		var userId = getFile.getUserId();
		var filename = getFile.getFilename();

		String fileId = String.format("%s_%s", userId, filename);

		synchronized (this) {
			this.files.put(fileId, getFile.getFileInfo());
		}
	}

	private void dir_writeFile(WriteFile writeFile) {
		var userId = writeFile.getUserId();
		var filename = writeFile.getFilename();
		var serverURIs = writeFile.getServerUris();

		String fileId = String.format("%s_%s", userId, filename);

		synchronized (this) {
			var file = files.get(fileId);
			this.files.put(fileId, writeFile.getFileInfo());

			if (file == null) {
				Set<URI> fileURIs = ConcurrentHashMap.newKeySet();
				for (URI serverURI : serverURIs)
					fileURIs.add(URI.create(String.format("%s%s/%s", serverURI, RestFiles.PATH, fileId)));
				file = new FileInfo(userId, filename, fileURIs.toArray()[0].toString(), ConcurrentHashMap.newKeySet());
				this.URIsPerFile.put(fileId, fileURIs);
			}

			this.files.put(fileId, file);

			var listFiles = accessibleFilesPerUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
			listFiles.add(file);
		}
	}

	private void dir_deleteFile(DeleteFile deleteFile) {
		var userId = deleteFile.getUserId();
		var filename = deleteFile.getFilename();

		synchronized (this) {
			var fileId = String.format("%s_%s", userId, filename);
			var file = files.get(fileId);
			this.files.put(fileId, deleteFile.getFileInfo());

			this.files.remove(fileId);
			this.URIsPerFile.remove(fileId);

			for (String user : file.getSharedWith()) {
				this.accessibleFilesPerUser.get(user).remove(file);
			}
		}
	}

	private void dir_shareFile(ShareFile shareFile) {
		var userId = shareFile.getUserId();
		var userIdShare = shareFile.getUserIdShare();
		var filename = shareFile.getFilename();

		synchronized (this) {
			var fileId = String.format("%s_%s", userId, filename);
			var file = files.get(fileId);

			file.getSharedWith().add(userIdShare);

			var listFiles = accessibleFilesPerUser.computeIfAbsent(userIdShare, k -> ConcurrentHashMap.newKeySet());
			listFiles.add(file);
		}
	}

	private void dir_unshareFile(UnshareFile unshareFile) {
		var userId = unshareFile.getUserId();
		var userIdShare = unshareFile.getUserIdShare();
		var filename = unshareFile.getFilename();

		synchronized (this) {
			var fileId = String.format("%s_%s", userId, filename);
			var file = files.get(fileId);

			file.getSharedWith().remove(userIdShare);
			if (!userId.equals(userIdShare)) {
				accessibleFilesPerUser.get(userIdShare).remove(file);
			}
		}
	}

	private Set<URI> intersectionWithDiscoveryOfFiles(FileInfo file) {
		URI[] discovered = Discovery.getInstance().knownUrisOf("files");
		String fileId = String.format("%s_%s", file.getOwner(), file.getFilename());

		Set<URI> uris = URIsPerFile.get(fileId);
		Set<URI> intersection = uris.stream().filter(uri -> {
			for (URI discoveredURI : discovered) {
				if (uri.toString().contains(discoveredURI.toString())) {
					return true;
				}
			}
			return false;
		}).collect(Collectors.toSet());

		if (intersection.size() != uris.size()) {
			file.setFileURL(intersection.toArray()[0].toString());
		}

		return intersection;
	}
}
