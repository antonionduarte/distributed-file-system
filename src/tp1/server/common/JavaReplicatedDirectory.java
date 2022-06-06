package tp1.server.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import tp1.api.FileInfo;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.server.operations.*;
import util.kafka.KafkaSubscriber;
import util.kafka.RecordProcessor;
import util.kafka.sync.SyncPoint;

import java.net.URI;
import java.util.List;

public class JavaReplicatedDirectory extends AbstractJavaDirectory implements Directory, RecordProcessor {


	private static final String DIRECTORY_REPLICATION_TOPIC = "directory_replication";

	private final SyncPoint<String> syncPoint;
	private final Gson gson;

	public JavaReplicatedDirectory(SyncPoint<String> syncPoint) {
		super();
		this.syncPoint = syncPoint;
		this.gson = new GsonBuilder().create();
		KafkaSubscriber sub = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(DIRECTORY_REPLICATION_TOPIC, DELETE_USER_TOPIC), FROM_BEGINNING);
		sub.start(false, this);
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
		String fileId = String.format("%s_%s", userId, filename);

		var p = beforeWriteFile(fileId, data, userId, password);
		var res = p.first();
		if (!res.isOK())
			return res;

		var writeFile = new WriteFile(filename, userId, p.second(), files.get(fileId));
		var version = pub.publish(DIRECTORY_REPLICATION_TOPIC, OperationType.WRITE_FILE.name(), gson.toJson(writeFile));
		this.syncPoint.waitForResult(version);

		return Result.ok(files.get(fileId));
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) {
		String fileId = String.format("%s_%s", userId, filename);

		var res = beforeDeleteFile(fileId, userId, password);
		if (!res.isOK())
			return res;

		var deleteFile = new DeleteFile(filename, userId);
		var version = pub.publish(DIRECTORY_REPLICATION_TOPIC, OperationType.DELETE_FILE.name(), gson.toJson(deleteFile));
		this.syncPoint.waitForResult(version);

		return Result.ok();
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
		var res = beforeShareOrUnshareFile(filename, userId, userIdShare, password);
		if (!res.isOK())
			return res;

		var shareFile = new ShareFile(filename, userId, userIdShare);
		var version = pub.publish(DIRECTORY_REPLICATION_TOPIC, OperationType.SHARE_FILE.name(), gson.toJson(shareFile));
		this.syncPoint.waitForResult(version);

		return Result.ok();
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
		var res = beforeShareOrUnshareFile(filename, userId, userIdShare, password);
		if (!res.isOK())
			return res;

		var unshareFile = new UnshareFile(filename, userId, userIdShare);
		var version = pub.publish(DIRECTORY_REPLICATION_TOPIC, OperationType.UNSHARE_FILE.name(), gson.toJson(unshareFile));
		this.syncPoint.waitForResult(version);

		return Result.ok();
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
		String fileId = String.format("%s_%s", userId, filename);
		FileInfo file = files.get(fileId);

		var res = beforeGetFile(file, userId, accUserId, password);
		if (!res.isOK())
			return res;

		var getFile = new GetFile(filename, userId, file);
		var version = pub.publish(DIRECTORY_REPLICATION_TOPIC, OperationType.GET_FILE.name(), gson.toJson(getFile));
		this.syncPoint.waitForResult(version);

		file = files.get(fileId);
		return Result.ok(URI.create(file.getFileURL()));
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
		} else {
			super.onReceive(record);
		}
	}
}
