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
				'}';
	}

	private final String password;

	public DeleteFile(String filename, String userId, String password) {
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
}
