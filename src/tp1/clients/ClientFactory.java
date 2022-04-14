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

import java.net.MalformedURLException;

public class ClientFactory {

	private static final Discovery discovery = Discovery.getInstance();

	public static Users getUsersClient() throws MalformedURLException {
		var serverURI = discovery.knownUrisOf("users").get(0); // use discovery to find a uri of the Users service;
		if (serverURI.toString().endsWith("rest"))
			return new RestUsersClient(serverURI);
		else
			return new SoapUsersClient(serverURI);
	}

	public static Directory getDirectoryClient() throws MalformedURLException {
		var serverURI = discovery.knownUrisOf("directory").get(0); // use discovery to find a uri of the Users service;
		if (serverURI.toString().endsWith("rest"))
			return new RestDirectoryClient(serverURI);
		else
			return new SoapDirectoryClient(serverURI);
	}

	public static Files getFilesClient() throws MalformedURLException {
		var serverURI = discovery.knownUrisOf("files").get(0); // use discovery to find a uri of the Users service;
		if (serverURI.toString().endsWith("rest"))
			return new RestFilesClient(serverURI);
		else
			return new SoapFilesClient(serverURI);
	}
}
