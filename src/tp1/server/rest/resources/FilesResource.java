package tp1.server.rest.resources;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Files;
import tp1.server.JavaFiles;
import util.ConvertError;

@Singleton
public class FilesResource implements RestFiles {

	final Files impl = new JavaFiles();

	public FilesResource() {
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
	public byte[] getFile(String fileId, String token) {
		var result = impl.getFile(fileId, token);

		if (result.isOK()) {
			return result.value();
		} else {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}
}
