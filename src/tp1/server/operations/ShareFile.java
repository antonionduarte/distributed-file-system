package tp1.server.operations;

public class ShareFile {

	private final String filename;
	private final String userId;
	private final String userIdShare;

	public ShareFile(String filename, String userId, String userIdShare) {
		this.filename = filename;
		this.userId = userId;
		this.userIdShare = userIdShare;
	}

	@Override
	public String toString() {
		return "ShareFile{" +
				"filename='" + filename + '\'' +
				", userId='" + userId + '\'' +
				", userIdShare='" + userIdShare + '\'' +
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
}
