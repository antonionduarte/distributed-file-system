package tp1.server.operations;

import tp1.api.FileInfo;

public class GetFile {

	private final String filename;
	private final String userId;
	private final String accUserId;
	private final String password;
	private final FileInfo fileInfo;

	@Override
	public String toString() {
		return "GetFile{" +
				"filename='" + filename + '\'' +
				", userId='" + userId + '\'' +
				", accUserId='" + accUserId + '\'' +
				", password='" + password + '\'' +
				", fileInfo=" + fileInfo +
				'}';
	}

	public GetFile(String filename, String userId, String accUserId, String password, FileInfo fileInfo) {
		this.filename = filename;
		this.userId = userId;
		this.accUserId = accUserId;
		this.password = password;
		this.fileInfo = fileInfo;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	public String getFilename() {
		return filename;
	}

	public String getAccUserId() {
		return accUserId;
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}
}
