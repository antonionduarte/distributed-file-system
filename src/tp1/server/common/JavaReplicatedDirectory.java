package tp1.server.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tp1.api.FileInfo;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.server.operations.*;
import util.Secret;
import util.Token;
import util.kafka.KafkaSubscriber;
import util.kafka.RecordProcessor;
import util.zookeeper.ReplicationManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static tp1.server.operations.OperationType.valueOf;

public class JavaReplicatedDirectory extends AbstractJavaDirectory implements Directory, RecordProcessor {

	private final ReplicationManager repManager;

	private final Gson gson;

	private final List<Operation> listOperations;

	private final static int TOLERABLE_FAILS_DIR = 1;

	public JavaReplicatedDirectory() {
		super();
		this.gson = new GsonBuilder().create();
		KafkaSubscriber sub = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(DELETE_USER_TOPIC), FROM_BEGINNING);
		sub.start(false, this);
		repManager = ReplicationManager.getInstance(this);
		listOperations = new LinkedList<>();
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
		if (repManager.isSecondary())
			return redirectToPrimary();

		String fileId = String.format("%s_%s", userId, filename);

		var p = beforeWriteFile(fileId, data, userId, password);
		var res = p.first();
		if (!res.isOK())
			return res;

		var writeFile = new WriteFile(filename, userId, p.second(), files.get(fileId));
		dir_writeFile(writeFile);
		repManager.incVersion();
		listOperations.add(writeFile);
		String operation = gson.toJson(writeFile);

		propagateOperationToSecondaries(operation, OperationType.WRITE_FILE);

		return Result.ok(files.get(fileId), repManager.getCurrentVersion());
	}


	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) {
		if (repManager.isSecondary())
			return redirectToPrimary();

		String fileId = String.format("%s_%s", userId, filename);

		var res = beforeDeleteFile(fileId, userId, password);
		if (!res.isOK())
			return res;

		var deleteFile = new DeleteFile(filename, userId);
		dir_deleteFile(deleteFile);
		repManager.incVersion();
		listOperations.add(deleteFile);
		String operation = gson.toJson(deleteFile);

		propagateOperationToSecondaries(operation, OperationType.DELETE_FILE);

		return Result.ok(repManager.getCurrentVersion());
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
		if (repManager.isSecondary())
			return redirectToPrimary();

		var res = beforeShareOrUnshareFile(filename, userId, userIdShare, password);
		if (!res.isOK())
			return res;

		var shareFile = new ShareFile(filename, userId, userIdShare);
		dir_shareFile(shareFile);
		repManager.incVersion();
		listOperations.add(shareFile);
		String operation = gson.toJson(shareFile);

		propagateOperationToSecondaries(operation, OperationType.SHARE_FILE);

		return Result.ok(repManager.getCurrentVersion());
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
		if (repManager.isSecondary())
			return redirectToPrimary();

		var res = beforeShareOrUnshareFile(filename, userId, userIdShare, password);
		if (!res.isOK())
			return res;

		var unshareFile = new UnshareFile(filename, userId, userIdShare);
		dir_unshareFile(unshareFile);
		repManager.incVersion();
		listOperations.add(unshareFile);
		String operation = gson.toJson(unshareFile);

		propagateOperationToSecondaries(operation, OperationType.UNSHARE_FILE);

		return Result.ok(repManager.getCurrentVersion());
	}

	@Override
	public Result<byte[]> getFile(Long version, String filename, String userId, String accUserId, String password) {
		long currentVersion = repManager.getCurrentVersion();
		if (currentVersion >= version) {
			var res = super.getFile(version, filename, userId, accUserId, password);
			return Result.ok(res.value(), currentVersion);
		}
		else
			return redirectToPrimary();
	}

	@Override
	public Result<List<FileInfo>> lsFile(Long version, String userId, String password) {
		long currentVersion = repManager.getCurrentVersion();
		if (currentVersion >= version) {
			var res = super.lsFile(version, userId, password);
			return Result.ok(res.value(), currentVersion);
		}
		else
			return redirectToPrimary();
	}

	@Override
	public Result<Void> opFromPrimary(Long version, String operation, String opType, String token) {
		if(Token.notValid(token, Secret.get()))
			return Result.error(Result.ErrorCode.FORBIDDEN);

		switch (valueOf(opType)) {
			case DELETE_FILE -> {
				DeleteFile op = gson.fromJson(operation, DeleteFile.class);
				listOperations.add(op);
				dir_deleteFile(op);
			}
			case SHARE_FILE -> {
				ShareFile op = gson.fromJson(operation, ShareFile.class);
				listOperations.add(op);
				dir_shareFile(op);
			}
			case UNSHARE_FILE -> {
				UnshareFile op = gson.fromJson(operation, UnshareFile.class);
				listOperations.add(op);
				dir_unshareFile(op);
			}
			case WRITE_FILE -> {
				WriteFile op = gson.fromJson(operation, WriteFile.class);
				listOperations.add(op);
				dir_writeFile(op);
			}
		}

		repManager.setVersion(version);

		return Result.ok();
	}

	@Override
	public Result<List<Operation>> getOperations(Long version, String token) {
		if(Token.notValid(token, Secret.get()))
			return Result.error(Result.ErrorCode.FORBIDDEN);

		List<Operation> missingOperations = listOperations.subList((int) Math.max((listOperations.size() - (Math.min(repManager.getCurrentVersion() - version, 0))), 0), listOperations.size());

		return Result.ok(missingOperations, repManager.getCurrentVersion());
	}

	private <T> Result<T> redirectToPrimary() {
		return Result.ok(repManager.getPrimaryURI());
	}


	private void propagateOperationToSecondaries(String operation, OperationType opType) {
		var clients = clientFactory.getOtherDirectoryClients();
		var theadFinishedSignal = new CountDownLatch(TOLERABLE_FAILS_DIR);
		for (Directory client: clients) {
			new Thread(() -> {
				var resultFromDir = client.opFromPrimary(repManager.getCurrentVersion(), operation, opType.name(), Token.generate(Secret.get()));
				if (resultFromDir != null && resultFromDir.isOK())
					theadFinishedSignal.countDown();
			}).start();
		}
		try {
			theadFinishedSignal.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void executeOperations(List<Operation> operations) {
		for (Operation operation : operations) {
			switch (operation.getOperationType()) {
				case DELETE_FILE -> dir_deleteFile((DeleteFile) operation);
				case SHARE_FILE -> dir_shareFile((ShareFile) operation);
				case UNSHARE_FILE -> dir_unshareFile((UnshareFile) operation);
				case WRITE_FILE -> dir_writeFile((WriteFile) operation);
			}
		}
	}

}
