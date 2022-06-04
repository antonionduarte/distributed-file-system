package tp1.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import tp1.api.FileInfo;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.server.operations.JsonOperation;
import tp1.server.operations.OperationType;
import tp1.server.operations.WriteFile;
import util.kafka.KafkaPublisher;
import util.kafka.KafkaSubscriber;
import util.kafka.RecordProcessor;
import util.kafka.sync.SyncPoint;

import java.net.MalformedURLException;
import java.util.List;

public class JavaReplicatedDirectory extends Thread implements Directory, RecordProcessor {
	static final String FROM_BEGINNING = "earliest";
	static final String TOPIC = "directory_replication";
	static final String KAFKA_BROKERS = "kafka:9092";

	//final String replicaId;
	final KafkaPublisher sender;
	final KafkaSubscriber receiver;

	private SyncPoint<String> syncPoint;

	private final Gson gson;

	public JavaReplicatedDirectory(SyncPoint<String> syncPoint) {
		this.sender = KafkaPublisher.createPublisher(KAFKA_BROKERS);
		this.receiver = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), FROM_BEGINNING);
		this.receiver.start(false, this);
		this.syncPoint = syncPoint;
		this.gson = new GsonBuilder().create();
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) throws MalformedURLException {


		var writeFile = new WriteFile(filename, data, userId, password);
		var json = gson.toJson(writeFile);
		var jsonOperation = new JsonOperation(json, OperationType.WRITE_FILE);




		return null;
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

	}
}
