package tp1.server.operations;

import tp1.api.FileInfo;

public class DeleteFile implements Operation {

	private String filename;
	private String userId;
	private String password;
	private FileInfo fileInfo;

	public DeleteFile(String filename, String userId, String password, FileInfo fileInfo) {
		this.filename = filename;
		this.userId = userId;
		this.password = password;
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

	public FileInfo getFileInfo() {
		return fileInfo;
	}
}
