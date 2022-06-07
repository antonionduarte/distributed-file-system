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
import tp1.server.common.JavaDirectory;
import tp1.server.operations.Operation;
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
	public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
		Result<FileInfo> result = impl.writeFile(filename, data, userId, password);

		if (result.isOK()) {
			return result.value();
		} else {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public void deleteFile(String filename, String userId, String password) {
		Result<Void> result = impl.deleteFile(filename, userId, password);

		if (!result.isOK()) {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public void shareFile(String filename, String userId, String userIdShare, String password) {
		Result<Void> result = impl.shareFile(filename, userId, userIdShare, password);

		if (!result.isOK()) {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public void unshareFile(String filename, String userId, String userIdShare, String password) {
		Result<Void> result = impl.unshareFile(filename, userId, userIdShare, password);

		if (!result.isOK()) {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public byte[] getFile(Long version, String filename, String userId, String accUserId, String password) {
		Result<byte[]> result;
		result = impl.getFile(version, filename, userId, accUserId, password);

		if (result.isOK()) {

			String fileId = userId + "_" + filename;
			if (result.redirectURI().toString().contains("/soap/")) {
				Files filesClient = clientFactory.getFilesClient(result.redirectURI()).second();

				Result<byte[]> resultFiles = filesClient.getFile(-1L, fileId, Token.generate(Secret.get(), fileId));
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
				throw new WebApplicationException(Response.temporaryRedirect(uriWithToken).header(HEADER_VERSION, result.version()).build());
			}
		} else {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public List<FileInfo> lsFile(Long version, String userId, String password) {
		Result<List<FileInfo>> result = impl.lsFile(version, userId, password);

		if (result.isOK()) {
			return result.value();
		} else {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public void opFromPrimary(Long version, String operation, String opType, String token) {
		Result<Void> result = impl.opFromPrimary(version, operation, opType, token);

		if (!result.isOK()) { //should always be FORBIDDEN
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public List<Operation> getOperations(Long version, String token) {
		throw new WebApplicationException(Response.Status.FORBIDDEN);
	}
}
