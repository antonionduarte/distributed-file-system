package tp1.clients;

import util.Debug;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Level;

public class DeleteUserClient {
	static final String USER_URI = "http://%s:%s";
	static final String PORT = "8080";

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static void main(String[] args) throws IOException {
		Debug.setLogLevel(Level.FINE, Debug.SD2122);

		if (args.length != 2) {
			System.err.println( "Use: java sd2122.aula2.clients.DeleteUserClient userId password");
			return;
		}
		
		String userId = args[0];
		String password = args[1];

		String ip = InetAddress.getLocalHost().getHostAddress();
		String userURI = String.format(USER_URI, ip, PORT);

		System.out.println("Sending request to server.");

		URI serverURI = DiscoveryHelper.findServiceURI(userId,userURI);
		var result = new RestUsersClient(serverURI).deleteUser(userId, password);
		System.out.println("Result: " + result);
	}
	
}
