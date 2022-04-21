package tp1.server;

import tp1.api.FileInfo;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.ClientFactory;
import util.Pair;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class JavaDirectory implements Directory {

	// String: filename
	private final Map<String, FileInfo> files;

	// String: userId
	private final Map<String, Set<FileInfo>> filesPerUser;

	private final ClientFactory clientFactory;

	public JavaDirectory() {
		this.files = new HashMap<>();
		this.filesPerUser = new HashMap<>();
		this.clientFactory = ClientFactory.getInstance();
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) throws MalformedURLException {
		String fileId = String.format("%s_%s", userId, filename);

		FileInfo file = files.get(fileId);

		if (file != null) {
			if (!file.getOwner().equals(userId)) {
				return Result.error(Result.ErrorCode.FORBIDDEN);
			}
		}

		Pair<String, Users> usersUriAndClient = clientFactory.getUsersClient();
		Users usersClient = usersUriAndClient.second();
		var userResult = usersClient.getUser(userId, password);

		// authenticate the user
		if (!userResult.isOK()) {
			return Result.error(userResult.error());
		}

		Pair<String, Files> filesUriAndClient = clientFactory.getFilesClient();
		String serverURI = filesUriAndClient.first();
		Files filesClient = filesUriAndClient.second();

		var filesResult = filesClient.writeFile(fileId, data, "");

		if (!filesResult.isOK()) {
			return Result.error(filesResult.error());
		}

		if (file == null) {
			String fileURL = String.format("%s%s/%s", serverURI, RestFiles.PATH, fileId);
			file = new FileInfo(userId, filename, fileURL, new HashSet<>());
		}

		files.put(fileId, file);

		var listFiles = filesPerUser.computeIfAbsent(userId, k -> new HashSet<>());
		listFiles.add(file);

		return Result.ok(file);
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) throws MalformedURLException {
		String fileId = String.format("%s_%s", userId, filename);

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

		// authenticate the user
		if (!userResult.isOK()) {
			return Result.error(userResult.error());
		}

		files.remove(fileId);
		filesPerUser.get(userId).remove(file);

		Files filesClient = clientFactory.getFilesClient().second();
		var filesResult = filesClient.deleteFile(fileId, "");

		if (!filesResult.isOK()) {
			return Result.error(filesResult.error());
		}
		
		return Result.ok();
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) throws MalformedURLException {
		String fileId = String.format("%s_%s", userId, filename);

		FileInfo file = files.get(fileId);

		if (file == null) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		Users usersClient = clientFactory.getUsersClient().second();
		var userResult = usersClient.getUser(userId, password);
		var userShareResult = usersClient.getUser(userIdShare, "");

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
		filesPerUser.get(userIdShare).remove(file);

		return Result.ok();
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) throws MalformedURLException {
		String fileId = String.format("%s_%s", userId, filename);

		FileInfo file = files.get(fileId);

		if (file == null) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		Users usersClient = clientFactory.getUsersClient().second();
		var userResult = usersClient.getUser(userId, password);
		var userShareResult = usersClient.getUser(userIdShare, "");

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

		var listFiles = filesPerUser.computeIfAbsent(userIdShare, k -> new HashSet<>());
		listFiles.add(file);

		return Result.ok();
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) throws MalformedURLException {
		String fileId = String.format("%s_%s", userId, filename);

		FileInfo file = files.get(fileId);

		if (file == null) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		Users usersClient = clientFactory.getUsersClient().second();
		var userResult = usersClient.getUser(userId, "");

		// check if userid exists
		if (userResult.error() == Result.ErrorCode.NOT_FOUND) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		var accUserResult = usersClient.getUser(accUserId, password);

		// authenticate the user
		if (!accUserResult.isOK()) {
			return Result.error(accUserResult.error());
		}
		// file not shared with user
		if (!file.getSharedWith().contains(accUserId) && !file.getOwner().equals(accUserId)) {
			return Result.error(Result.ErrorCode.FORBIDDEN);
		}
		// wrong path
		if (!file.getOwner().equals(userId)) {
			return Result.error(Result.ErrorCode.BAD_REQUEST);
		}

		try {
			URI resourceURI = new URI(file.getFileURL());
			return Result.ok(resourceURI);
		} catch (URISyntaxException e) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password) {
		Users usersClient;
		try {
			usersClient = clientFactory.getUsersClient().second();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
		var userResult = usersClient.getUser(userId, password);

		// check if userid exists
		if (!userResult.isOK()) {
			return Result.error(userResult.error());
		}

		return Result.ok(new LinkedList<>(filesPerUser.get(userId)));
	}
}
