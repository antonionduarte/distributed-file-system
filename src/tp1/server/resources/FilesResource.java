package tp1.server.resources;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.service.rest.RestFiles;

import java.io.*;
import java.util.logging.Logger;

@Singleton
public class FilesResource implements RestFiles {

	private static final Logger Log = Logger.getLogger(UsersResource.class.getName());

	public FilesResource() {}

	@Override
	public void writeFile(String fileId, byte[] data, String token) {
		Log.info("writeFile : "+fileId);

		File file = new File(fileId);
		try {
			if(file.createNewFile())
				Log.info("File created.");
			else
				Log.info("Writing on existing .");
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(data);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
	}

	@Override
	public void deleteFile(String fileId, String token) {
		Log.info("deleteFile : "+fileId);

		File file = new File(fileId);
		if(file.delete()) {
			Log.info("File deleted.");
		} else
			throw new WebApplicationException(Response.Status.NOT_FOUND);
	}

	@Override
	public byte[] getFile(String fileId, String token) {
		Log.info("getFile : "+fileId);

		File file = new File(fileId);
		try {
			FileInputStream fis = new FileInputStream(file);
			return fis.readAllBytes();
		} catch (FileNotFoundException e) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
	}
}
