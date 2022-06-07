package tp1.server.operations;

public class DeleteFile extends Operation {

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
		super(OperationType.DELETE_FILE);
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
