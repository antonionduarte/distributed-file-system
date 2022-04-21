package tp1.clients;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.ws.rs.client.Client;
import tp1.api.User;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ClientFactory {

	private static final Discovery discovery = Discovery.getInstance();

	private static final int CACHE_DURATION = 10; // 10 seconds for now

	Cache<String, Pair<String, Users>> usersCache;
	Cache<String, Pair<String, Files>> filesCache;
	Cache<String, Pair<String, Directory>> directoryCache;

	public static ClientFactory getInstance() {
		if (instance == null) {
			instance = new ClientFactory();
		}
		return instance;
	}

	private static ClientFactory instance;

	public ClientFactory() {
		this.usersCache = CacheBuilder.newBuilder().expireAfterAccess(CACHE_DURATION, TimeUnit.SECONDS).build();
		this.filesCache = CacheBuilder.newBuilder().expireAfterAccess(CACHE_DURATION, TimeUnit.SECONDS).build();
		this.directoryCache = CacheBuilder.newBuilder().expireAfterAccess(CACHE_DURATION, TimeUnit.SECONDS).build();
	}

	public Pair<String, Users> getUsersClient() throws MalformedURLException, ExecutionException {
		var serverURI = discovery.knownUrisOf("users").get(0);

		return this.usersCache.get(serverURI.toString(), () -> {
			if (serverURI.toString().endsWith("rest")) {
				return new Pair<>(serverURI.toString(), new RestUsersClient(serverURI));
			} else {
				return new Pair<>(serverURI.toString(), new SoapUsersClient(serverURI));
			}
		});
	}

	public Pair<String, Directory> getDirectoryClient() throws MalformedURLException, ExecutionException {
		var serverURI = discovery.knownUrisOf("directory").get(0); // use discovery to find an uri of the Users service;

		return this.directoryCache.get(serverURI.toString(), () -> {
			if (serverURI.toString().endsWith("rest")) {
				return new Pair<>(serverURI.toString(), new RestDirectoryClient(serverURI));
			} else {
				return new Pair<>(serverURI.toString(), new SoapDirectoryClient(serverURI));
			}
		});
	}

	public Pair<String, Files> getFilesClient() throws MalformedURLException, ExecutionException {
		var serverURI = discovery.knownUrisOf("files").get(0); // use discovery to find an uri of the Users service;

		return this.filesCache.get(serverURI.toString(), () -> {
			if (serverURI.toString().endsWith("rest")) {
				return new Pair<>(serverURI.toString(), new RestFilesClient(serverURI));
			} else {
				return new Pair<>(serverURI.toString(), new SoapFilesClient(serverURI));
			}
		});
	}
}
