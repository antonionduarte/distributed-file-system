package tp1.server.operations;

public class UnshareFile implements Operation {

	private final String filename;
	private final String userId;
	private final String userIdShare;
	private final String password;

	public UnshareFile(String filename, String userId, String userIdShare, String password) {
		this.filename = filename;
		this.userId = userId;
		this.userIdShare = userIdShare;
		this.password = password;
	}

	public String getFilename() {
		return filename;
	}

	public String getPassword() {
		return password;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserIdShare() {
		return userIdShare;
	}
}
