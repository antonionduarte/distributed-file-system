package tp1.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import util.ConvertError;

import java.net.URI;

import static tp1.api.service.rest.RestDirectory.HEADER_VERSION;

@SuppressWarnings("unchecked")
public class RestFilesClient extends RestClient implements Files {

	final WebTarget target;

	public RestFilesClient(URI serverURI) {
		super(serverURI);
		target = client.target(serverURI).path(RestFiles.PATH);
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		return super.reTry(() -> clt_writeFile(fileId, data, token));
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		return super.reTry(() -> clt_deleteFile(fileId, token));
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		return super.reTry(() -> clt_getFile(fileId, token));
	}

	private Result<Void> clt_writeFile(String fileId, byte[] data, String token) {
		Response response = target
				.path(fileId)
				.queryParam(RestFiles.TOKEN, token)
				.request()
				.post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}

	private Result<Void> clt_deleteFile(String fileId, String token) {
		Response response = target
				.path(fileId)
				.queryParam(RestFiles.TOKEN, token)
				.request()
				.delete();

		if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok();
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}

	private Result<byte[]> clt_getFile(String fileId, String token) {
		Response response = target
				.path(fileId)
				.queryParam(RestFiles.TOKEN, token)
				.request()
				.accept(MediaType.APPLICATION_OCTET_STREAM)
				.get();

		if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity()) {
			return Result.ok(response.readEntity(byte[].class));
		}

		return ConvertError.webAppErrorToResultError(response.getStatusInfo().toEnum());
	}
}
