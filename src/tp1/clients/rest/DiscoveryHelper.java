package tp1.clients.rest;

import java.net.URI;
import java.util.ArrayList;

import tp1.server.rest.UsersServer;
import util.Discovery;

public class DiscoveryHelper {

	public static URI findServiceURI() {
		Discovery discovery = Discovery.getInstance();
		discovery.listener();
		ArrayList<URI> serverURIs = discovery.knownUrisOf(UsersServer.SERVICE);
		return serverURIs.get(0);
	}

}
