package tp1.server.common;

import tp1.api.FileInfo;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.server.operations.*;
import util.kafka.KafkaSubscriber;
import util.kafka.RecordProcessor;

import java.net.URI;
import java.util.List;

public class JavaDirectory extends AbstractJavaDirectory implements Directory, RecordProcessor {


	public JavaDirectory() {
		KafkaSubscriber sub = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(DELETE_USER_TOPIC), FROM_BEGINNING);
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
		dir_writeFile(writeFile);

		return Result.ok(files.get(fileId));
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) {
		String fileId = String.format("%s_%s", userId, filename);

		var res = beforeDeleteFile(fileId, userId, password);
		if (!res.isOK())
			return res;

		var deleteFile = new DeleteFile(filename, userId);
		dir_deleteFile(deleteFile);

		return Result.ok();
	}


	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
		var res = beforeShareOrUnshareFile(filename, userId, userIdShare, password);
		if (!res.isOK())
			return res;

		var shareFile = new ShareFile(filename, userId, userIdShare);
		dir_shareFile(shareFile);

		return Result.ok();
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
		var res = beforeShareOrUnshareFile(filename, userId, userIdShare, password);
		if (!res.isOK())
			return res;

		var unshareFile = new UnshareFile(filename, userId, userIdShare);
		dir_unshareFile(unshareFile);

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
		dir_getFile(getFile);

		return Result.ok(URI.create(file.getFileURL()));
	}
}
