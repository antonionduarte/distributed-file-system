package tp1.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.server.operations.Operation;
import util.ConvertError;

import java.net.URI;
import java.util.List;

@SuppressWarnings("unchecked")
public class RestDirectoryClient extends RestClient implements Directory {

	final WebTarget target;

	public RestDirectoryClient(URI serverURI) {
		super(serverURI);
		target = client.target(serverURI).path(RestDirectory.PATH);
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
		return super.reTry(() -> clt_writeFile(filename, data, userId, password));
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) {
		return super.reTry(() -> clt_deleteFile(filename, userId, password));
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
		return super.reTry(() -> clt_shareFile(filename, userId, userIdShare, password));
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
		return super.reTry(() -> clt_unshareFile(filename, userId, userIdShare, password));
	}

	@Override
	public Result<byte[]> getFile(Long version, String filename, String userId, String accUserId, String password) {
		return super.reTry(() -> clt_getFile(version, filename, userId, accUserId, password));
	}

	@Override
	public Result<List<FileInfo>> lsFile(Long version, String userId, String password) {
		return super.reTry(() -> clt_lsFile(version, userId, password));

	}

	@Override
	public Result<Void> opFromPrimary(Long version, String operation, String opType, String token) {
		return super.reTry(() -> {
			Response response = target
					.queryParam("opType", opType)
					.queryParam(RestDirectory.TOKEN, token)
					.request()
					.header(RestDirectory.HEADER_VERSION, version)
					.post(Entity.entity(operation, MediaType.APPLICATION_JSON));

			if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity()) {
				return Result.ok();
			}

			return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
		});
	}

	@Override
	public Result<List<Operation>> getOperations(Long version, String token) {
		return null;
	}

	private Result<FileInfo> clt_writeFile(String filename, byte[] data, String userId, String password) {
		Response response = target.path(userId).path(filename)
				.queryParam(RestUsers.PASSWORD, password)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

		if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok(response.readEntity(FileInfo.class));
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());

	}

	private Result<Void> clt_deleteFile(String filename, String userId, String password) {
		Response response = target.path(userId).path(filename)
				.queryParam(RestUsers.PASSWORD, password)
				.request()
				.delete();

		if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok();
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}

	private Result<Void> clt_shareFile(String filename, String userId, String userIdShare, String password) {
		Response response = target.path(userId).path(filename).path(userIdShare)
				.queryParam(RestUsers.PASSWORD, password)
				.request()
				.post(null); //probably something else?

		if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok();
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}

	private Result<Void> clt_unshareFile(String filename, String userId, String userIdShare, String password) {
		Response response = target.path(userId).path(filename).path(userIdShare)
				.queryParam(RestUsers.PASSWORD, password)
				.request()
				.delete();

		if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok();
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}

	private Result<byte[]> clt_getFile(Long version, String filename, String userId, String accUserId, String password) {
		Response response = target
				.path(userId).path(filename)
				.queryParam("accUserId", accUserId)
				.queryParam(RestUsers.PASSWORD, password)
				.request()
				.header(RestDirectory.HEADER_VERSION, version)
				.accept(MediaType.APPLICATION_OCTET_STREAM)
				.get();

		if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok(response.readEntity(byte[].class));
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}

	private Result<List<FileInfo>> clt_lsFile(Long version, String userId, String password) {
		Response response = target
				.path(userId)
				.queryParam(RestUsers.PASSWORD, password)
				.request()
				.header(RestDirectory.HEADER_VERSION, version)
				.accept(MediaType.APPLICATION_JSON)
				.get();

		if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok((List<FileInfo>) response.readEntity(new GenericType<>() {
			}));
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}
}
