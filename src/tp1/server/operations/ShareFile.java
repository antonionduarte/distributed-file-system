package tp1.server.operations;

public class ShareFile {

	private final String filename;
	private final String userId;
	private final String userIdShare;
	private final String password;

	public ShareFile(String filename, String userId, String userIdShare, String password) {
		this.filename = filename;
		this.userId = userId;
		this.userIdShare = userIdShare;
		this.password = password;
	}

	@Override
	public String toString() {
		return "ShareFile{" +
				"filename='" + filename + '\'' +
				", userId='" + userId + '\'' +
				", userIdShare='" + userIdShare + '\'' +
				", password='" + password + '\'' +
				'}';
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
