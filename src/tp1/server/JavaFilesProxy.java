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

import java.util.logging.Logger;

public class JavaFilesProxy implements Files {

	private static final Logger Log = Logger.getLogger(JavaFiles.class.getName());

	// TODO: Change this to program args
	private static final String API_KEY = "2v5aoett5ga8tec";
	private static final String API_SECRET = "1qw5p1vin7d07r2";
	private static final String ACCESS_TOKEN_STR = "sl.BIdt5A8FVj9EDA9yo7a248yEchTwYDYZ7-nfnVHsHk1dDOiMXFucXhV3jDBdbHPoGhXQBBd8z_4PKGOIPKIvFpgo6Uy1b1E9o9551YKaqPv8sdd4GtYnJXCEyQV9ARrEccGviY4";

	private static final String UPLOAD_FILE_V2_URL = "https://content.dropboxapi.com/2/files/upload";
	private static final String DELETE_FILE_V2_URL = "https://api.dropboxapi.com/2/files/delete_v2";
	private static final String DOWNLOAD_FILE_V2_URL = "https://content.dropboxapi.com/2/files/download";

	private static final int HTTP_SUCCESS = 200;
	private static final int HTTP_NOT_FOUND = 409;

	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String DROPBOX_API_ARG_HDR = "Dropbox-API-Arg";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	private static final String OCTET_STREAM_CONTENT_TYPE = "application/octet-stream";

	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;

	public JavaFilesProxy() {
		this.json = new Gson();
		this.accessToken = new OAuth2AccessToken(ACCESS_TOKEN_STR);
		this.service = new ServiceBuilder(API_KEY).apiSecret(API_SECRET).build(DropboxApi20.INSTANCE);
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		Log.info("writeFile : " + fileId);

		var jsonArgs = json.toJson(new UploadFileV2Args(
				false,
				false,
				false,
				"/" + fileId,
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
		Log.info("deleteFile : " + fileId);

		var jsonArgs = json.toJson(new DeleteFileV2Args("/" + fileId));

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
		Log.info("getFile : " + fileId);

		var jsonArgs = json.toJson(new DownloadFileV2Args("/" + fileId));

		var downloadFile = new OAuthRequest(Verb.POST, DOWNLOAD_FILE_V2_URL);
		downloadFile.addHeader(CONTENT_TYPE_HDR, DROPBOX_API_ARG_HDR);
		downloadFile.setPayload(jsonArgs);

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
}
