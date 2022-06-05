package tp1.server.operations;

public class DeleteFile {

	private final String filename;
	private final String userId;

	@Override
	public String toString() {
		return "DeleteFile{" +
				"filename='" + filename + '\'' +
				", userId='" + userId + '\'' +
				'}';
	}

	public DeleteFile(String filename, String userId) {
		this.filename = filename;
		this.userId = userId;
	}

	public String getFilename() {
		return filename;
	}

	public String getUserId() {
		return userId;
	}
}
