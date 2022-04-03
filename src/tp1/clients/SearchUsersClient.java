package tp1.clients;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Level;

import util.Debug;

public class SearchUsersClient {
	static final String USER_URI = "http://%s:%s";
	static final String PORT = "8080";

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static void main(String[] args) throws IOException {
		Debug.setLogLevel(Level.FINE, Debug.SD2122);

		if (args.length != 1) {
			System.err.println("Use: java sd2122.aula3.clients.SearchUsersClient userId ");
			return;
		}

		String userId = args[0];

		String ip = InetAddress.getLocalHost().getHostAddress();
		String userURI = String.format(USER_URI, ip, PORT);

		System.out.println("Sending request to server.");

		URI serverURI = DiscoveryHelper.findServiceURI(userId, userURI);
		var result = new RestUsersClient(URI.create(serverURI.toString())).searchUsers(userId);
		System.out.println("Result: " + result);
	}
}
