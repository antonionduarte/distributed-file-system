package tp1.server;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.ClientFactory;
import util.Discovery;
import util.Pair;
import util.Secret;
import util.Token;
import util.kafka.KafkaSubscriber;
import util.kafka.RecordProcessor;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class JavaDirectory implements Directory, RecordProcessor {

	private static final String FROM_BEGINNING = "earliest";
	private static final String KAFKA_BROKERS = "kafka:9092";
	private static final String DELETE_USER_TOPIC = "delete_user";

	// String: filename
	private final Map<String, FileInfo> files;

	// String: fileId
	private final Map<String, Set<URI>> URIsPerFile;

	// String: userId
	private final Map<String, Set<FileInfo>> accessibleFilesPerUser;

	private final ClientFactory clientFactory;

	public JavaDirectory() {
		this.files = new ConcurrentHashMap<>();
		this.accessibleFilesPerUser = new ConcurrentHashMap<>();
		this.clientFactory = ClientFactory.getInstance();
		this.URIsPerFile = new ConcurrentHashMap<>();

		KafkaSubscriber sub = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(DELETE_USER_TOPIC), FROM_BEGINNING);
		sub.start(false, this);
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
		String fileId = String.format("%s_%s", userId, filename);

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

			Set<URI> serverURIs = new HashSet<>();
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

			files.remove(fileId);
			URIsPerFile.remove(fileId);
		}

		accessibleFilesPerUser.get(userId).remove(file);
		for (String user : file.getSharedWith()) {
			accessibleFilesPerUser.get(user).remove(file);
		}

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

		file.getSharedWith().remove(userIdShare);
		if (!userId.equals(userIdShare)) {
			accessibleFilesPerUser.get(userIdShare).remove(file);
		}

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

		file.getSharedWith().add(userIdShare);

		var listFiles = accessibleFilesPerUser.computeIfAbsent(userIdShare, k -> ConcurrentHashMap.newKeySet());
		listFiles.add(file);

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
		return Result.ok(URI.create(file.getFileURL()));
	}

	@Override
	public synchronized Result<List<FileInfo>> lsFile(String userId, String password) {
		Users usersClient = clientFactory.getUsersClient().second();
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

	@Override
	public void onReceive(ConsumerRecord<String, String> record) {
		removeUserInfo(record.value());
	}
}
