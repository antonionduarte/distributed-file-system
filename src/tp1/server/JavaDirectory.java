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
import java.util.concurrent.ConcurrentHashMap;

public class JavaDirectory implements Directory {

	// String: filename
	private final Map<String, FileInfo> files;

	// String: userId
	private final Map<String, Set<FileInfo>> accessibleFilesPerUser;

	private final ClientFactory clientFactory;

	public JavaDirectory() {
		this.files = new ConcurrentHashMap<>();
		this.accessibleFilesPerUser = new ConcurrentHashMap<>();
		this.clientFactory = ClientFactory.getInstance();
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) throws MalformedURLException {
		String fileId = String.format("%s_%s", userId, filename);

		FileInfo file;
		synchronized (this) {
			file = files.get(fileId);

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

			Pair<String, Files> filesUriAndClient;

			if (file == null) {
				filesUriAndClient = clientFactory.getFilesClient();
			} else {
				filesUriAndClient = clientFactory.getFilesClient(file.getFileURL());
			}

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
		}


		var listFiles = accessibleFilesPerUser.computeIfAbsent(userId, k -> new HashSet<>());
		listFiles.add(file);

		return Result.ok(file);
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) throws MalformedURLException {
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

			// authenticate the user
			if (!userResult.isOK()) {
				return Result.error(userResult.error());
			}

			Files filesClient = clientFactory.getFilesClient(file.getFileURL()).second();
			var filesResult = filesClient.deleteFile(fileId, "");

			if (!filesResult.isOK()) {
				return Result.error(filesResult.error());
			}

			files.remove(fileId);
		}

		accessibleFilesPerUser.get(userId).remove(file);
		for (String user : file.getSharedWith()) {
			accessibleFilesPerUser.get(user).remove(file);
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
		if(!userId.equals(userIdShare))
			accessibleFilesPerUser.get(userIdShare).remove(file);

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

		var listFiles = accessibleFilesPerUser.computeIfAbsent(userIdShare, k -> new HashSet<>());
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

		// authenticate userId
		if (!userResult.isOK()) {
			return Result.error(userResult.error());
		}

		var list = accessibleFilesPerUser.get(userId);
		if(list == null)
			return Result.ok(new LinkedList<>());
		else
			return Result.ok(new LinkedList<>(list));

	}

	@Override
	public Result<Void> removeUser(String userId) {

		var listFiles = accessibleFilesPerUser.remove(userId);
		if(listFiles == null)
			return Result.ok();

		for (FileInfo file : listFiles) {
			if(file.getOwner().equals(userId)) {
				//delete user's files from others accessible files
				for (String user : file.getSharedWith()) {
					if(!user.equals(userId))
						accessibleFilesPerUser.get(user).remove(file);
				}

				//delete user's files from files server
				//different files have different clients although same user
				Files filesClient = clientFactory.getFilesClient(file.getFileURL()).second();
				filesClient.deleteFile(file.getOwner()+"_"+file.getFilename(), "");
			}
			//delete user from shareWith of others files
			file.getSharedWith().remove(userId);
		}

		return Result.ok();
	}
}
