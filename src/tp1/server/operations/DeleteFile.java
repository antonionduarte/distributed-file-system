package tp1.server.operations;

import tp1.api.FileInfo;

public class DeleteFile {

	private final String filename;
	private final String userId;

	@Override
	public String toString() {
		return "DeleteFile{" +
				"filename='" + filename + '\'' +
				", userId='" + userId + '\'' +
				", password='" + password + '\'' +
				", fileInfo=" + fileInfo +
				'}';
	}

	private final String password;
	private final FileInfo fileInfo;

	public DeleteFile(String filename, String userId, String password, FileInfo fileInfo) {
		this.filename = filename;
		this.userId = userId;
		this.password = password;
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

	public FileInfo getFileInfo() {
		return fileInfo;
	}
}
