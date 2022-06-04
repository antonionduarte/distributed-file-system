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
import tp1.server.JavaReplicatedDirectory;
import util.ConvertError;
import util.Secret;
import util.Token;
import util.kafka.sync.SyncPoint;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

@Singleton
public class ReplicatedDirectoryResource implements RestDirectory {

	private final Directory impl;

	private final ClientFactory clientFactory = ClientFactory.getInstance();

	public ReplicatedDirectoryResource(SyncPoint<String> syncPoint) {
		this.impl = new JavaReplicatedDirectory(syncPoint);
	}

	@Override
	public FileInfo writeFile(Long version, String filename, byte[] data, String userId, String password) {
		Result<FileInfo> result;
		try {
			result = impl.writeFile(filename, data, userId, password);
		} catch (MalformedURLException e) {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (result.isOK()) {
			throw new WebApplicationException(Response.ok().header(HEADER_VERSION, result.version()).entity(result.value()).build());
		} else {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}

	@Override
	public void deleteFile(Long version, String filename, String userId, String password) {
		Result<Void> result;
		try {
			result = impl.deleteFile(filename, userId, password);
		} catch (MalformedURLException e) {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (!result.isOK()) {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
		throw new WebApplicationException(Response.noContent().header(HEADER_VERSION, result.version()).build());

	}

	@Override
	public void shareFile(Long version, String filename, String userId, String userIdShare, String password) {
		Result<Void> result;
		try {
			result = impl.shareFile(filename, userId, userIdShare, password);
		} catch (MalformedURLException e) {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (!result.isOK()) {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}

		throw new WebApplicationException(Response.noContent().header(HEADER_VERSION, result.version()).build());
	}

	@Override
	public void unshareFile(Long version, String filename, String userId, String userIdShare, String password) {
		Result<Void> result;
		try {
			result = impl.unshareFile(filename, userId, userIdShare, password);
		} catch (MalformedURLException e) {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (!result.isOK()) {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}

		throw new WebApplicationException(Response.noContent().header(HEADER_VERSION, result.version()).build());
	}

	@Override
	public byte[] getFile(Long version, String filename, String userId, String accUserId, String password) {
		Result<byte[]> result;
		try {
			result = impl.getFile(filename, userId, accUserId, password);
		} catch (MalformedURLException e) {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (result.isOK()) {

			String fileId = userId + "_" + filename;
			if (result.redirectURI().toString().contains("/soap/")) {
				Files filesClient = clientFactory.getFilesClient(result.redirectURI()).second();

				Result<byte[]> resultFiles = filesClient.getFile(fileId, Token.generate(Secret.get(), fileId));
				if (resultFiles == null) {
					throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
				}

				if (resultFiles.isOK()) {
					throw new WebApplicationException(Response.ok().header(HEADER_VERSION, result.version()).entity(result.value()).build());
				} else {
					throw new WebApplicationException(ConvertError.resultErrorToWebAppError(resultFiles));
				}
			} else {
				URI uriWithToken = URI.create(result.redirectURI().toString() + "?token=" + Token.generate(Secret.get(), fileId));
				throw new WebApplicationException(Response.temporaryRedirect(uriWithToken).header(HEADER_VERSION, result.version()).build());
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

	@Override
	public void removeUser(String userId, String token) {
		Result<Void> result = impl.removeUser(userId, token);

		if (!result.isOK()) {
			var errorCode = ConvertError.resultErrorToWebAppError(result);
			throw new WebApplicationException(errorCode);
		}
	}
}
