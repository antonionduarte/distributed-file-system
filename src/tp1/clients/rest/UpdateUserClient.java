package tp1.clients.rest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Level;

import tp1.api.User;

import util.Debug;

public class UpdateUserClient {
	static final String USER_URI = "http://%s:%s";
	static final String PORT = "8080";

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static void main(String[] args) throws IOException {
		Debug.setLogLevel(Level.FINE, Debug.SD2122);

		if (args.length != 5) {
			System.err.println("Use: java sd2122.aula2.clients.UpdateUserClient userId oldpwd fullName email password");
			return;
		}

		String userId = args[0];
		String oldpwd = args[1];
		String fullName = args[2];
		String email = args[3];
		String password = args[4];

		String ip = InetAddress.getLocalHost().getHostAddress();
		String userURI = String.format(USER_URI, ip, PORT);

		User user = new User(userId, fullName, email, password);

		System.out.println("Sending request to server.");

		URI serverURI = DiscoveryHelper.findServiceURI();
		var result = new RestUsersClient(serverURI).updateUser(userId, oldpwd, user);
		System.out.println("Result: " + result);
	}
}

