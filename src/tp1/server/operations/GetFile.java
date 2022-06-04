package tp1.server.operations;

public class GetFile {

	private String filename;
	private String userId;
	private String accUserId;
	private String password;

	public GetFile(String filename, String userId, String accUserId, String password) {
		this.filename = filename;
		this.userId = userId;
		this.accUserId = accUserId;
		this.password = password;
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
}
