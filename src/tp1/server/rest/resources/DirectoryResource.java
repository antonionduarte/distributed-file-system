package tp1.server.rest.resources;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.clients.ClientFactory;
import tp1.server.JavaDirectory;
import util.ConvertError;
import util.Secret;
import util.Token;

import java.net.URI;
import java.util.List;

@Singleton
public class DirectoryResource implements RestDirectory {

	private final Directory impl = new JavaDirectory();

	private final ClientFactory clientFactory = ClientFactory.getInstance();

	@Override
	public FileInfo writeFile(Long header, String filename, byte[] data, String userId, String password) {
		Result<FileInfo> result;
		result = impl.writeFile(filename, data, userId, password);

		if (result.isOK()) {
			return result.value();
		} else {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public void deleteFile(Long header, String filename, String userId, String password) {
		Result<Void> result;
		result = impl.deleteFile(filename, userId, password);

		if (!result.isOK()) {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public void shareFile(Long header, String filename, String userId, String userIdShare, String password) {
		Result<Void> result;
		result = impl.shareFile(filename, userId, userIdShare, password);

		if (!result.isOK()) {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public void unshareFile(Long header, String filename, String userId, String userIdShare, String password) {
		Result<Void> result;
		result = impl.unshareFile(filename, userId, userIdShare, password);

		if (!result.isOK()) {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public byte[] getFile(Long header, String filename, String userId, String accUserId, String password) {
		Result<byte[]> result;
		result = impl.getFile(filename, userId, accUserId, password);

		if (result.isOK()) {

			String fileId = userId + "_" + filename;
			if (result.redirectURI().toString().contains("/soap/")) {
				Files filesClient = clientFactory.getFilesClient(result.redirectURI()).second();

				Result<byte[]> resultFiles = filesClient.getFile(fileId, Token.generate(Secret.get(), fileId));
				if (resultFiles == null) {
					throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
				}

				if (resultFiles.isOK()) {
					return resultFiles.value();
				} else {
					throw new WebApplicationException(ConvertError.resultErrorToWebAppError(resultFiles));
				}
			} else {
				URI uriWithToken = URI.create(result.redirectURI().toString()+"?token="+ Token.generate(Secret.get(), fileId));
				throw new WebApplicationException(Response.temporaryRedirect(uriWithToken).build());
			}
		} else {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public List<FileInfo> lsFile(String userId, String password) {
		Result<List<FileInfo>> result = impl.lsFile(userId, password);

		if (result.isOK()) {
			return result.value();
		} else {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}
}
