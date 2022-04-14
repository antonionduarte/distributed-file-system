package tp1.server.rest.resources;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Files;
import tp1.server.JavaFiles;

import java.io.*;
import java.util.logging.Logger;

@Singleton
public class FilesResource implements RestFiles {

	final Files impl = new JavaFiles();

	public FilesResource() {
	}

	@Override
	public void writeFile(String fileId, byte[] data, String token) {
		var result = impl.writeFile(fileId, data, token);
		if(!result.isOK())
			throw new WebApplicationException();

	}

	@Override
	public void deleteFile(String fileId, String token) {
		var result = impl.deleteFile(fileId, token);
		if(!result.isOK())
			throw new WebApplicationException();
	}

	@Override
	public byte[] getFile(String fileId, String token) {
		var result = impl.getFile(fileId, token);
		if(result.isOK())
			return result.value();
		else
			throw new WebApplicationException();
	}
}
