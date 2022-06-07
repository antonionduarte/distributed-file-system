package tp1.server.rest.resources;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Files;
import tp1.server.common.JavaFilesProxy;
import util.ConvertError;

import static tp1.api.service.rest.RestDirectory.HEADER_VERSION;

@Singleton
public class FilesProxyResource implements RestFiles {

	private final Files impl;

	public FilesProxyResource(boolean deleteAll, String apiKey, String apiSecret, String accessKey) {
		this.impl = new JavaFilesProxy(deleteAll, apiKey, apiSecret, accessKey);
	}

	@Override
	public void writeFile(String fileId, byte[] data, String token) {
		var result = impl.writeFile(fileId, data, token);

		if (!result.isOK()) {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		} else {
			throw new WebApplicationException(Response.Status.NO_CONTENT);
		}
	}

	@Override
	public void deleteFile(String fileId, String token) {
		var result = impl.deleteFile(fileId, token);

		if (!result.isOK()) {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public byte[] getFile(Long version, String fileId, String token) {
		var result = impl.getFile(version, fileId, token);

		if (result.isOK()) {
			throw new WebApplicationException(Response.ok().header(HEADER_VERSION, result.version()).entity(result.value()).build());
		} else {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}
}
