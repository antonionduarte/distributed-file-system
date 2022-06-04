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
import tp1.server.operations.JsonOperation;
import tp1.server.operations.OperationType;
import tp1.server.operations.WriteFile;
import util.Discovery;
import util.Pair;
import util.Secret;
import util.Token;
import util.kafka.KafkaPublisher;
import util.kafka.KafkaSubscriber;
import util.kafka.RecordProcessor;
import util.kafka.sync.SyncPoint;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JavaReplicatedDirectory extends Thread implements Directory, RecordProcessor {

	// String: filename
	private final Map<String, FileInfo> files;

	// String: fileId
	private final Map<String, Set<URI>> URIsPerFile;

	// String: userId
	private final Map<String, Set<FileInfo>> accessibleFilesPerUser;

	private final ClientFactory clientFactory;

	static final String FROM_BEGINNING = "earliest";
	static final String TOPIC = "directory_replication";
	static final String KAFKA_BROKERS = "localhost:9092";

	//final String replicaId;
	final KafkaPublisher sender;
	final KafkaSubscriber receiver;

	private SyncPoint<String> syncPoint;

	Gson gson;

	public JavaReplicatedDirectory(SyncPoint<String> syncPoint) {
		this.sender = KafkaPublisher.createPublisher(KAFKA_BROKERS);
		this.receiver = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), FROM_BEGINNING);
		this.receiver.start(false, this);
		this.syncPoint = syncPoint;
		this.gson = new GsonBuilder().create();
		this.files = new ConcurrentHashMap<>();
		this.accessibleFilesPerUser = new ConcurrentHashMap<>();
		this.clientFactory = ClientFactory.getInstance();
		this.URIsPerFile = new ConcurrentHashMap<>();
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) throws MalformedURLException {
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

			//if (file == null) {
			//	Set<URI> fileURIs = ConcurrentHashMap.newKeySet();
			//	for (URI serverURI : serverURIs)
			//		fileURIs.add(URI.create(String.format("%s%s/%s", serverURI, RestFiles.PATH, fileId)));
			//	file = new FileInfo(userId, filename, fileURIs.toArray()[0].toString(), ConcurrentHashMap.newKeySet());
			//	URIsPerFile.put(fileId, fileURIs);
			//}

			//files.put(fileId, file);
		}

		var listFiles = accessibleFilesPerUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
		listFiles.add(file);

		var writeFile = new WriteFile(filename, data, userId, password);
		var jsonOperation = new JsonOperation(writeFile, OperationType.WRITE_FILE);

		var version = sender.publish(TOPIC, gson.toJson(jsonOperation));
		var result = syncPoint.waitForResult(version);

		return Result.ok(file);
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) throws MalformedURLException {


		return null;
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) throws MalformedURLException {
		return null;
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) throws MalformedURLException {
		return null;
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) throws MalformedURLException {
		return null;
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password) {
		return null;
	}

	@Override
	public Result<Void> removeUserFiles(String userId, String token) {
		return null;
	}

	@Override
	public void onReceive(ConsumerRecord<String, String> record) {
		var version = record.offset();
		var result = record.value();
		this.syncPoint.setResult(version, result);

		JsonOperation jsonOperation = gson.fromJson(result, JsonOperation.class);

		switch (jsonOperation.getOperationType()) {
			case WRITE_FILE -> /* call file */
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
