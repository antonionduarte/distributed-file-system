package tp1.server.operations;

import tp1.api.FileInfo;

import java.net.URI;
import java.util.Set;

public class WriteFile extends Operation  {

	private final String filename;
	private final String userId;
	private final Set<URI> serverUris;
	private final FileInfo fileInfo;

	@Override
	public String toString() {
		return "WriteFile{" +
				"filename='" + filename + '\'' +
				", userId='" + userId + '\'' +
				", serverUris=" + serverUris +
				", fileInfo=" + fileInfo +
				'}';
	}

	public WriteFile(String filename, String userId, Set<URI> serverUris, FileInfo fileInfo) {
		super(OperationType.WRITE_FILE);
		this.filename = filename;
		this.userId = userId;
		this.serverUris = serverUris;
		this.fileInfo = fileInfo;
	}

	public String getFilename() {
		return filename;
	}

	public String getUserId() {
		return userId;
	}

	public Set<URI> getServerUris() {
		return serverUris;
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}
}
