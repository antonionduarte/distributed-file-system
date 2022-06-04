package tp1.server.operations;

public class LsFile implements Operation {

	private String userId;
	private String password;

	public LsFile(String userId, String password) {
		this.userId = userId;
		this.password = password;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}
}
