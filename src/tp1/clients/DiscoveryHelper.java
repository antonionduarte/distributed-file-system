package tp1.clients;

import java.net.URI;
import java.util.ArrayList;

import tp1.server.Discovery;
import tp1.server.UsersServer;

public class DiscoveryHelper {

	public static URI findServiceURI(String userID, String userURI) {
		Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, userID, userURI);
		discovery.listener();
		ArrayList<URI> serverURIs = discovery.knownUrisOf(UsersServer.SERVICE);
		return serverURIs.get(0);
	}

}
