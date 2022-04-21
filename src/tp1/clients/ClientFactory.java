package tp1.clients;

import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Users;
import tp1.clients.rest.RestDirectoryClient;
import tp1.clients.rest.RestFilesClient;
import tp1.clients.rest.RestUsersClient;
import tp1.clients.soap.SoapDirectoryClient;
import tp1.clients.soap.SoapFilesClient;
import tp1.clients.soap.SoapUsersClient;
import util.Discovery;
import util.Pair;

import java.net.MalformedURLException;
import java.net.URI;

public class ClientFactory {

	private static final Discovery discovery = Discovery.getInstance();

	public static Pair<String, Users> getUsersClient() throws MalformedURLException {
		var serverURI = discovery.knownUrisOf("users").get(0); // use discovery to find a uri of the Users service;
		if (serverURI.toString().endsWith("rest"))
			return new Pair<>(serverURI.toString(), new RestUsersClient(serverURI));
		else
			return new Pair<>(serverURI.toString(), new SoapUsersClient(serverURI));
	}

	public static Pair<String, Directory> getDirectoryClient() throws MalformedURLException {
		var serverURI = discovery.knownUrisOf("directory").get(0); // use discovery to find a uri of the Users service;
		if (serverURI.toString().endsWith("rest"))
			return new Pair<>(serverURI.toString(), new RestDirectoryClient(serverURI));
		else
			return new Pair<>(serverURI.toString(), new SoapDirectoryClient(serverURI));
	}

	public static Pair<String, Files> getFilesClient() throws MalformedURLException {
		var serverURI = discovery.knownUrisOf("files").get(0); // use discovery to find a uri of the Users service;
		if (serverURI.toString().endsWith("rest"))
			return new Pair<>(serverURI.toString(), new RestFilesClient(serverURI));
		else
			return new Pair<>(serverURI.toString(), new SoapFilesClient(serverURI));
	}
}
