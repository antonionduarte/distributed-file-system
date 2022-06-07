package tp1.server.operations;

public class UnshareFile extends Operation  {

	private final String filename;
	private final String userId;
	private final String userIdShare;

	@Override
	public String toString() {
		return "UnshareFile{" +
				"filename='" + filename + '\'' +
				", userId='" + userId + '\'' +
				", userIdShare='" + userIdShare + '\'' +
				'}';
	}

	public UnshareFile(String filename, String userId, String userIdShare) {
		super(OperationType.UNSHARE_FILE);
		this.filename = filename;
		this.userId = userId;
		this.userIdShare = userIdShare;
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
