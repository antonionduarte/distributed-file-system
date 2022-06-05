package tp1.server;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import org.pac4j.scribe.builder.api.DropboxApi20;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.server.dropbox.DeleteFileV2Args;
import tp1.server.dropbox.DownloadFileV2Args;
import tp1.server.dropbox.UploadFileV2Args;
import util.Secret;
import util.Token;

import java.util.logging.Logger;

public class JavaFilesProxy implements Files {

	private static final Logger Log = Logger.getLogger(JavaFiles.class.getName());

	private static final String UPLOAD_FILE_V2_URL = "https://content.dropboxapi.com/2/files/upload";
	private static final String DELETE_FILE_V2_URL = "https://api.dropboxapi.com/2/files/delete_v2";
	private static final String DOWNLOAD_FILE_V2_URL = "https://content.dropboxapi.com/2/files/download";

	private static final int HTTP_SUCCESS = 200;
	private static final int HTTP_NOT_FOUND = 409;

	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String DROPBOX_API_ARG_HDR = "Dropbox-API-Arg";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	private static final String OCTET_STREAM_CONTENT_TYPE = "application/octet-stream";

	private static final String ROOT = "/distributed-fs";
	private static final String DELIMITER = "_";

	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;

	public JavaFilesProxy(boolean deleteAll, String apiSecret, String accessKey, String apiKey) {
		this.json = new Gson();
		this.accessToken = new OAuth2AccessToken(accessKey);
		this.service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);

		if (deleteAll) {
			this.deleteAll("");
		}
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		fileId = fileId.replace( DELIMITER, "/");

		Log.info("writeFile : " + fileId);

		if (!Token.validate(token, Secret.get(), fileId)) {
			return Result.error(Result.ErrorCode.FORBIDDEN);
		}

		var jsonArgs = json.toJson(new UploadFileV2Args(
				false,
				false,
				false,
				ROOT + "/" + fileId,
				"overwrite"
		));

		var writeFile = new OAuthRequest(Verb.POST, UPLOAD_FILE_V2_URL);
		writeFile.addHeader(DROPBOX_API_ARG_HDR, jsonArgs);
		writeFile.addHeader(CONTENT_TYPE_HDR, OCTET_STREAM_CONTENT_TYPE);
		writeFile.setPayload(data);

		service.signRequest(accessToken, writeFile);

		try {
			var response = service.execute(writeFile);

			if (response.getCode() != HTTP_SUCCESS) {
				return Result.error(Result.ErrorCode.INTERNAL_ERROR);
			}
		} catch (Exception e) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

		return Result.ok();
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		fileId = fileId.replace( DELIMITER, "/");

		Log.info("deleteFile : " + fileId);


		if (!Token.validate(token, Secret.get(), fileId)) {
			return Result.error(Result.ErrorCode.FORBIDDEN);
		}

		String path = ROOT;
		if (!fileId.equals("")) {
			path += "/" + fileId;
		}

		var jsonArgs = json.toJson(new DeleteFileV2Args(path));

		var deleteFile = new OAuthRequest(Verb.POST, DELETE_FILE_V2_URL);
		deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		deleteFile.setPayload(jsonArgs);

		service.signRequest(accessToken, deleteFile);

		try {
			var response = service.execute(deleteFile);

			if (response.getCode() != HTTP_SUCCESS) {
				if (response.getCode() == HTTP_NOT_FOUND) {
					return Result.error(Result.ErrorCode.NOT_FOUND);
				} else {
					return Result.error(Result.ErrorCode.INTERNAL_ERROR);
				}
			}
		} catch (Exception e) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

		return Result.ok();
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		fileId = fileId.replace( DELIMITER, "/");

		Log.info("getFile : " + fileId);

		if (!Token.validate(token, Secret.get(), fileId)) {
			return Result.error(Result.ErrorCode.FORBIDDEN);
		}

		var jsonArgs = json.toJson(new DownloadFileV2Args(ROOT + "/" + fileId));

		var downloadFile = new OAuthRequest(Verb.POST, DOWNLOAD_FILE_V2_URL);
		downloadFile.addHeader(DROPBOX_API_ARG_HDR, jsonArgs);
		downloadFile.addHeader(CONTENT_TYPE_HDR, OCTET_STREAM_CONTENT_TYPE);

		service.signRequest(accessToken, downloadFile);

		try {
			var response = service.execute(downloadFile);

			if (response.getCode() != HTTP_SUCCESS) {
				if (response.getCode() == HTTP_NOT_FOUND) {
					return Result.error(Result.ErrorCode.NOT_FOUND);
				} else {
					return Result.error(Result.ErrorCode.INTERNAL_ERROR);
				}
			}

			return Result.ok(response.getStream().readAllBytes());
		} catch (Exception e) {
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String token) {
		if (!Token.validate(token, Secret.get(), userId)) {
			return Result.error(Result.ErrorCode.FORBIDDEN);
		}

		deleteAll(userId);

		return Result.ok();
	}

	private void deleteAll(String folder) {

		var jsonArgs = json.toJson(new DeleteFileV2Args(ROOT + "/" + folder));

		var deleteFolder = new OAuthRequest(Verb.POST, DELETE_FILE_V2_URL);
		deleteFolder.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		deleteFolder.setPayload(jsonArgs);

		service.signRequest(accessToken, deleteFolder);

		try {
			service.execute(deleteFolder);
		} catch (Exception e) {
			// do nothing
		}
	}

	/*
	public static void main(String[] args) {
		JavaFilesProxy javaFilesProxy = new JavaFilesProxy(
				true,
				"1qw5p1vin7d07r2",
				"sl.BIwj-NhPvbbpSBSp4yJig7tet4av0P_x5PMd5IuauqIzPbuiUD-dhRwdA-Y3ujUq-xHOn5Fw8ARuvASBPLAzbgbbgLzvDdS06-OD_F3-_XA4CAxMhHlh3jkh91oM-NSx_ywLomM",
				"2v5aoett5ga8tec"
		);

		File file = new File("./Dockerfile");

		byte[] data = new byte[0];
		try (FileInputStream fis = new FileInputStream(file)) {
			data = fis.readAllBytes();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//var response = javaFilesProxy.deleteFile("", "");
		//System.out.println(response.toString());
		//var response = javaFilesProxy.writeFile("Dockerfile", data, "");
		//System.out.println(response.toString());

		// javaFilesProxy.getFile("ANT_OMEGALUL_NI_OMEGALUL", "");
	}
	 */
}
