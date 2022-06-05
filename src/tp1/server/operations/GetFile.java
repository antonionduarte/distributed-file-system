package tp1.server.operations;

import tp1.api.FileInfo;

public class GetFile {

	private final String filename;
	private final String userId;
	private final FileInfo fileInfo;

	@Override
	public String toString() {
		return "GetFile{" +
				"filename='" + filename + '\'' +
				", userId='" + userId + '\'' +
				", fileInfo=" + fileInfo +
				'}';
	}

	public GetFile(String filename, String userId, FileInfo fileInfo) {
		this.filename = filename;
		this.userId = userId;
		this.fileInfo = fileInfo;
	}

	public String getUserId() {
		return userId;
	}

	public String getFilename() {
		return filename;
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}
}
