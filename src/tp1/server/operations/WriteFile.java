package tp1.server.operations;

import tp1.api.FileInfo;

import java.net.URI;
import java.util.Set;

public class WriteFile implements Operation {

	private final String filename;
	private final String userId;
	private final String password;

	private final byte[] data;

	private final Set<URI> serverUris;
	private final FileInfo fileInfo;

	public WriteFile(String filename, byte[] data, String userId, String password, Set<URI> serverUris, FileInfo fileInfo) {
		this.filename = filename;
		this.data = data;
		this.userId = userId;
		this.password = password;
		this.serverUris = serverUris;
		this.fileInfo = fileInfo;
	}

	public String getFilename() {
		return filename;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	public Set<URI> getServerUris() {
		return serverUris;
	}

	public byte[] getData() {
		return data;
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}
}
