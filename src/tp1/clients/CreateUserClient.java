package tp1.clients;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import tp1.api.User;

import util.Debug;

public class CreateUserClient {
	static final String USER_URI = "http://%s:%s";
	static final String PORT = "8080";

	private static final Logger Log = Logger.getLogger(CreateUserClient.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static void main(String[] args) throws IOException {
		Debug.setLogLevel(Level.FINE, Debug.SD2122);

		if (args.length != 4) {
			System.err.println("Use: java sd2122.aula3.clients.CreateUserClient userId fullName email password");
			return;
		}

		String userId = args[0];
		String fullName = args[1];
		String email = args[2];
		String password = args[3];

		String ip = InetAddress.getLocalHost().getHostAddress();
		String userURI = String.format(USER_URI, ip, PORT);

		User user = new User(userId, fullName, email, password);

		Log.info("Sending request to server.");

		URI serverURI = DiscoveryHelper.findServiceURI(userId, userURI);
		var result = new RestUsersClient(serverURI).createUser(user);
		System.out.println("Result: " + result);
	}
}
