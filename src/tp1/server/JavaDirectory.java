package tp1.server;

import tp1.api.FileInfo;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.ClientFactory;
import util.Pair;

import java.net.MalformedURLException;
import java.util.*;

public class JavaDirectory implements Directory {

	//String: filename
	private Map<String, FileInfo> files;


	public JavaDirectory() {
		files = new HashMap<>();
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) throws MalformedURLException {
		FileInfo file = files.get(filename);

		if (file != null) {
			if (!file.getOwner().equals(userId)) {
				return Result.error(Result.ErrorCode.FORBIDDEN);
			}
		}

		Pair<String, Users> usersUriAndClient = ClientFactory.getUsersClient();
		Users usersClient = usersUriAndClient.second();
		var userResult = usersClient.getUser(userId, password);

		// authenticate the user
		if (!userResult.isOK()) {
			return Result.error(userResult.error());
		}

		Pair<String, Files> filesUriAndClient = ClientFactory.getFilesClient();
		String serverURI = filesUriAndClient.first();
		Files filesClient = filesUriAndClient.second();

		var filesResult = filesClient.writeFile(filename, data, "");

		if (!filesResult.isOK()) {
			return Result.error(Result.ErrorCode.BAD_REQUEST);
		}

		if (file == null) {

			String fileURL = String.format("%s%s%s", serverURI,"/files/",filename);
			file = new FileInfo(userId, filename, fileURL, new HashSet<>());
		}

		files.put(filename, file);
		return Result.ok(file);
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) throws MalformedURLException {
		FileInfo file = files.get(filename);

		if (file != null) {
			if (!file.getOwner().equals(userId)) {
				return Result.error(Result.ErrorCode.FORBIDDEN);
			}
		} else {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		Users usersClient = ClientFactory.getUsersClient();
		var userResult = usersClient.getUser(userId, password);

		// authenticate the user
		if (!userResult.isOK()) {
			return Result.error(userResult.error());
		}

		return Result.ok();
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) throws MalformedURLException {
		FileInfo file = files.get(filename);

		if (file != null) {
			if (!file.getOwner().equals(userId)) {
				return Result.error(Result.ErrorCode.FORBIDDEN);
			}
		} else {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		Users usersClient = ClientFactory.getUsersClient();
		var userResult = usersClient.getUser(userId, password);

		// authenticate the user
		if (!userResult.isOK()) {
			return Result.error(userResult.error());
		}

		// TODO: Issues I described on Discord

		return Result.ok();
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) throws MalformedURLException {
		FileInfo file = files.get(filename);

		if (file != null) {
			if (!file.getOwner().equals(userId)) {
				return Result.error(Result.ErrorCode.FORBIDDEN);
			}
		} else {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		Users usersClient = ClientFactory.getUsersClient();
		var userResult = usersClient.getUser(userId, password);

		// authenticate the user
		if (!userResult.isOK()) {
			return Result.error(userResult.error());
		}

		// TODO: Issues I described on Discord

		return Result.ok();
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) throws MalformedURLException {
		FileInfo file = files.get(filename);

		if (file != null) {
			if (!file.getOwner().equals(userId)) {
				return Result.error(Result.ErrorCode.FORBIDDEN);
			}
		} else {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		Users usersClient = ClientFactory.getUsersClient();
		var userResult = usersClient.getUser(userId, password);

		// authenticate the user
		if (!userResult.isOK()) {
			return Result.error(userResult.error());
		}

		// TODO: Get the file from the correct server and stuff...
		return null;
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password) {
		return null;
	}
}
