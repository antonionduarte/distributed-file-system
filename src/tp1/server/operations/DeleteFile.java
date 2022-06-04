package tp1.server.operations;

public class DeleteFile implements Operation {

	private String filename;
	private String userId;
	private String password;

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
