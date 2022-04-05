package tp1.clients.rest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Level;

import util.Debug;

public class GetUserClient {
	static final String USER_URI = "http://%s:%s";
	static final String PORT = "8080";

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static void main(String[] args) throws IOException {
		Debug.setLogLevel(Level.FINE, Debug.SD2122);

		if (args.length != 2) {
			System.err.println("Use: java sd2122.aula2.clients.GetUserClient userId password");
			return;
		}

		String userId = args[0];
		String password = args[1];

		String ip = InetAddress.getLocalHost().getHostAddress();
		String userURI = String.format(USER_URI, ip, PORT);

		System.out.println("Sending request to server.");

		URI serverURI = DiscoveryHelper.findServiceURI();
		var result = new RestUsersClient(serverURI).getUser(userId, password);
		System.out.println("Result: " + result);
	}
}
