package tp1.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;

import java.net.URI;

public class RestFilesClient extends RestClient implements Files {

	final WebTarget target;

	public RestFilesClient(URI serverURI) {
		super(serverURI);
		target = client.target(serverURI).path(RestUsers.PATH);
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		super.reTry(() -> clt_writeFile(fileId, data, token));
		return Result.ok();
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		super.reTry(() -> clt_deleteFile(fileId, token));
		return Result.ok();
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		return super.reTry(() -> Result.ok(clt_getFile(fileId, token)));
	}

	private boolean clt_writeFile(String fileId, byte[] data, String token) {
		Response response = target
				.path(fileId)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

		if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity())
			return true;
		else
			System.out.println("Error, HTTP error status: " + response.getStatus());

		return false;

	}

	private boolean clt_deleteFile(String fileId, String token) {
		Response response = target
				.path(fileId)
				.request()
				.delete();

		if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity())
			return true;
		else
			System.out.println("Error, HTTP error status: " + response.getStatus());

		return false;
	}

	private byte[] clt_getFile(String fileId, String token) {
		Response response = target
				.path(fileId)
				.request()
				.accept(MediaType.APPLICATION_OCTET_STREAM)
				.get();

		if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity())
			return response.readEntity(byte[].class);
		else
			return null;
	}
}
