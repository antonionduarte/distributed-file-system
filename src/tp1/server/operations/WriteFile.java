package tp1.server.operations;

import java.net.URI;
import java.util.Set;

public class WriteFile implements Operation {

	private String filename;
	private String userId;
	private String password;

	private byte[] data;

	private Set<URI> serverUris;

	public WriteFile(String filename, byte[] data, String userId, String password, Set<URI> serverUris) {
		this.filename = filename;
		this.data = data;
		this.userId = userId;
		this.password = password;
		this.serverUris = serverUris;
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

	public Set<URI> getServerUris() {
		return serverUris;
	}

	public byte[] getData() {
		return data;
	}
}
