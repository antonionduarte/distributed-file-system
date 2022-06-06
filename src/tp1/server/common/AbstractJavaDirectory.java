package tp1.server.common;

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
import util.kafka.RecordProcessor;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public abstract class AbstractJavaDirectory implements Directory, RecordProcessor {

	// String: fileId
	protected final Map<String, FileInfo> files;
	protected final Map<String, Set<URI>> URIsPerFile;

	// String: userId
	protected final Map<String, Set<FileInfo>> accessibleFilesPerUser;

	protected final ClientFactory clientFactory;

	protected static final String FROM_BEGINNING = "earliest";
	protected static final String DELETE_USER_TOPIC = "delete_user";
	protected static final String DELETE_FILE_TOPIC = "delete_file"; //in order to delete files from down file servers
	protected static final String KAFKA_BROKERS = "kafka:9092";

	protected final KafkaPublisher pub;

	public AbstractJavaDirectory() {
		this.pub = KafkaPublisher.createPublisher(KAFKA_BROKERS);
		this.files = new ConcurrentHashMap<>();
		this.accessibleFilesPerUser = new ConcurrentHashMap<>();
		this.clientFactory = ClientFactory.getInstance();
		this.URIsPerFile = new ConcurrentHashMap<>();
	}


	protected Pair<Result<FileInfo>, Set<URI>> beforeWriteFile(String fileId, byte[] data, String userId, String password) {
		FileInfo file = files.get(fileId);
		if (file != null && !file.getOwner().equals(userId)) {
			return new Pair<>(Result.error(Result.ErrorCode.FORBIDDEN), null);
		}

		Pair<URI, Users> usersUriAndClient = clientFactory.getUsersClient();
		Users usersClient = usersUriAndClient.second();
		var userResult = usersClient.getUser(userId, password);

		if (userResult == null) {
			return new Pair<>(Result.error(Result.ErrorCode.INTERNAL_ERROR), null);
		}

		// authenticate the user
		if (!userResult.isOK()) {
			return new Pair<>(Result.error(userResult.error()), null);
		}

		Set<Pair<URI, Files>> filesUrisAndClients = new HashSet<>();

		if (file == null) {
			filesUrisAndClients = clientFactory.getFilesClients();
		} else {
			for (URI uri : intersectionWithDiscoveryOfFiles(file, false)) {
				var client = clientFactory.getFilesClient(uri);
				if (client != null) {
					filesUrisAndClients.add(client);
				}
			}
		}

		Set<URI> serverURIs = new HashSet<>();
		Result<Void> filesResult = null;
		for (Pair<URI, Files> filesUriAndClient : filesUrisAndClients) {
			serverURIs.add(filesUriAndClient.first());
			Files filesClient = filesUriAndClient.second();

			filesResult = filesClient.writeFile(fileId, data, Token.generate(Secret.get(), fileId));
			if (!filesResult.isOK()) {
				return new Pair<>(Result.error(filesResult.error()), null);
			}
		}

		if (filesResult == null) {
			return new Pair<>(Result.error(Result.ErrorCode.INTERNAL_ERROR), null);
		}


		return new Pair<>(Result.ok(), serverURIs);
	}

	protected Result<Void> beforeDeleteFile(String fileId, String userId, String password) {
		FileInfo file = files.get(fileId);

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
		for (URI fileURI : intersectionWithDiscoveryOfFiles(file, false)) {
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

		pub.publish(DELETE_FILE_TOPIC, fileId);

		return Result.ok();
	}

	protected Result<Void> beforeShareOrUnshareFile(String filename, String userId, String userIdShare, String password) {
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

		return Result.ok();
	}

	protected Result<byte[]> beforeGetFile(FileInfo file, String userId, String accUserId, String password) {
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

		intersectionWithDiscoveryOfFiles(file, true);

		return Result.ok();
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

	protected void dir_getFile(GetFile getFile) {
		var userId = getFile.getUserId();
		var filename = getFile.getFilename();

		String fileId = String.format("%s_%s", userId, filename);

		synchronized (this) {
			this.files.put(fileId, getFile.getFileInfo());
		}
	}

	protected void dir_writeFile(WriteFile writeFile) {
		var userId = writeFile.getUserId();
		var filename = writeFile.getFilename();
		var serverURIs = writeFile.getServerUris();

		String fileId = String.format("%s_%s", userId, filename);

		synchronized (this) {
			var file = writeFile.getFileInfo();

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

	protected void dir_deleteFile(DeleteFile deleteFile) {
		var userId = deleteFile.getUserId();
		var filename = deleteFile.getFilename();

		synchronized (this) {
			var fileId = String.format("%s_%s", userId, filename);

			var file = this.files.remove(fileId);
			this.URIsPerFile.remove(fileId);

			accessibleFilesPerUser.get(userId).remove(file);
			for (String user : file.getSharedWith()) {
				this.accessibleFilesPerUser.get(user).remove(file);
			}
		}
	}

	protected void dir_shareFile(ShareFile shareFile) {
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

	protected void dir_unshareFile(UnshareFile unshareFile) {
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

	protected void removeUserInfo(String userId) {
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

	protected Set<URI> intersectionWithDiscoveryOfFiles(FileInfo file, boolean changeFileURL) {
		String fileId = String.format("%s_%s", file.getOwner(), file.getFilename());

		Set<URI> uris = URIsPerFile.get(fileId);
		Set<URI> intersection;
		do {
			URI[] discovered = Discovery.getInstance().knownUrisOf("files");
			intersection = uris.stream().filter(uri -> {
				for (URI discoveredURI : discovered) {
					if (uri.toString().contains(discoveredURI.toString())) {
						return true;
					}
				}
				return false;
			}).collect(Collectors.toSet());
		} while (intersection.isEmpty());

		if (intersection.size() != uris.size() && changeFileURL) {
			file.setFileURL(intersection.toArray()[0].toString());
		}

		return intersection;
	}

	@Override
	public void onReceive(ConsumerRecord<String, String> record) {
		removeUserInfo(record.value());
	}
}
