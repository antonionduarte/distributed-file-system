package tp1.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.rest.RestUsers;

import java.net.URI;

public class RestFilesClient extends RestClient implements RestFiles {

	final WebTarget target;

	public RestFilesClient(URI serverURI) {
		super(serverURI);
		target = client.target(serverURI).path(RestUsers.PATH);
	}

	@Override
	public void writeFile(String fileId, byte[] data, String token) {
		super.reTry(() -> clt_writeFile(fileId, data, token));
	}

	@Override
	public void deleteFile(String fileId, String token) {
		super.reTry(() -> clt_deleteFile(fileId, token));
	}

	@Override
	public byte[] getFile(String fileId, String token) {
		return super.reTry(() -> clt_getFile(fileId, token));
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
