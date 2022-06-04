package tp1.server;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import tp1.api.FileInfo;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import util.kafka.RecordProcessor;
import util.kafka.sync.SyncPoint;

import java.net.MalformedURLException;
import java.util.List;

public class JavaReplicatedDirectory implements Directory, RecordProcessor {

	private SyncPoint<String> syncPoint;

	public JavaReplicatedDirectory(SyncPoint<String> syncPoint) {
		this.syncPoint = syncPoint;
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) throws MalformedURLException {
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
