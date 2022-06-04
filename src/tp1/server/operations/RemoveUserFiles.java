package tp1.server.operations;

public class RemoveUserFiles implements Operation {

	private String userId;
	private String token;

	public RemoveUserFiles(String userId, String token) {
		this.userId = userId;
		this.token = token;
	}

	public String getUserId() {
		return userId;
	}

	public String getToken() {
		return token;
	}
}
