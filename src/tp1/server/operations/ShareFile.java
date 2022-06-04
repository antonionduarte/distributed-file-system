package tp1.server.operations;

public class ShareFile implements Operation {

	private String filename;
	private String userId;
	private String userIdShare;
	private String password;

	public ShareFile(String filename, String userId, String userIdShare, String password) {
		this.filename = filename;
		this.userId = userId;
		this.userIdShare = userIdShare;
		this.password = password;
	}

	public String getFilename() {
		return filename;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserIdShare() {
		return userIdShare;
	}

	public String getPassword() {
		return password;
	}
}
