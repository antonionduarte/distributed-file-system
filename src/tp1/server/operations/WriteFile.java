package tp1.server.operations;

public class WriteFile {

	private String filename;
	private String userId;
	private String password;

	private byte[] data;

	public WriteFile(String filename, byte[] data, String userId, String password) {
		this.filename = filename;
		this.data = data;
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

	public byte[] getData() {
		return data;
	}
}
