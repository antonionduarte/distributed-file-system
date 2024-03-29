package tp1.clients;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Users;
import tp1.clients.rest.RestDirectoryClient;
import tp1.clients.rest.RestFilesClient;
import tp1.clients.rest.RestUsersClient;
import tp1.clients.soap.SoapDirectoryClient;
import tp1.clients.soap.SoapFilesClient;
import tp1.clients.soap.SoapUsersClient;
import tp1.server.rest.AbstractRestServer;
import util.Discovery;
import util.Pair;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ClientFactory {

	private static final int FILES_REPLICATION_FACTOR = 2;

	private static final Discovery discovery = Discovery.getInstance();

	private static final int CACHE_DURATION = 10; // 10 seconds for now
	private static ClientFactory instance;
	private final Cache<URI, Pair<URI, Users>> usersCache;
	private final Cache<URI, Pair<URI, Files>> filesCache;
	private final Cache<URI, Pair<URI, Directory>> directoryCache;
	private final Map<URI, Integer> distribution;

	public ClientFactory() {
		this.distribution = new ConcurrentHashMap<>();

		this.usersCache = CacheBuilder.newBuilder().expireAfterAccess(CACHE_DURATION, TimeUnit.SECONDS).build();
		this.filesCache = CacheBuilder.newBuilder().expireAfterAccess(CACHE_DURATION, TimeUnit.SECONDS).build();
		this.directoryCache = CacheBuilder.newBuilder().expireAfterAccess(CACHE_DURATION, TimeUnit.SECONDS).build();
	}

	public static ClientFactory getInstance() {
		if (instance == null) {
			instance = new ClientFactory();
		}
		return instance;
	}

	public Pair<URI, Users> getUsersClient() {
		var serverURI = discovery.knownUrisOf("users")[0];

		try {
			return this.usersCache.get(serverURI, () -> {
				if (serverURI.toString().endsWith("rest")) {
					return new Pair<>(serverURI, new RestUsersClient(serverURI));
				} else {
					return new Pair<>(serverURI, new SoapUsersClient(serverURI));
				}
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public Pair<URI, Directory> getDirectoryClient() {
		var serverURI = discovery.knownUrisOf("directory")[0];

		try {
			return this.directoryCache.get(serverURI, () -> {
				if (serverURI.toString().endsWith("rest")) {
					return new Pair<>(serverURI, new RestDirectoryClient(serverURI));
				} else {
					return new Pair<>(serverURI, new SoapDirectoryClient(serverURI));
				}
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<Directory> getOtherDirectoryClients() {
		var serverURIs = discovery.knownUrisOf("directory");
		Set<Directory> clients = new HashSet<>();
		for (URI serverURI: serverURIs) {
			if(!serverURI.toString().equals(AbstractRestServer.SERVER_URI)) {
				try {
					clients.add(this.directoryCache.get(serverURI, () -> {
						if (serverURI.toString().endsWith("rest")) {
							return new Pair<>(serverURI, new RestDirectoryClient(serverURI));
						} else {
							return new Pair<>(serverURI, new SoapDirectoryClient(serverURI));
						}
					}).second());
				} catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return clients;
	}

	public Set<Pair<URI, Files>> getFilesClients() {
		var serverURIs = discovery.knownUrisOf("files");

		for (URI serverURI : serverURIs) {
			distribution.putIfAbsent(serverURI, 0);
		}

		var urisFiles = minFiles(serverURIs);

		Set<Pair<URI, Files>> clients = new HashSet<>();
		for (URI uri : urisFiles) {
			distribution.put(uri, distribution.get(uri) + 1);

			try {
				clients.add(this.filesCache.get(uri, () -> {
					if (uri.toString().endsWith("rest")) {
						return new Pair<>(uri, new RestFilesClient(uri));
					} else {
						return new Pair<>(uri, new SoapFilesClient(uri));
					}
				}));
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		return clients;
	}

	//returns STORE_IN_HOW_MANY servers with the lowest storage
	private Set<URI> minFiles(URI[] serverURIs) {
		Set<URI> uris = new HashSet<>(FILES_REPLICATION_FACTOR);

		for (int i = 0; i < FILES_REPLICATION_FACTOR; i++) {
			Map.Entry<URI, Integer> min = null;
			for (Map.Entry<URI, Integer> entry : distribution.entrySet()) {

				if (Arrays.stream(serverURIs).toList().contains(entry.getKey()) && (min == null || min.getValue() > entry.getValue()) && !uris.contains(entry.getKey())) {
					min = entry;
				}
			}

			if (min != null)
				uris.add(min.getKey());
			else
				break;
		}

		return uris;
	}

	public Pair<URI, Files> getFilesClient(URI resourceURI) {
		URI serverURI = URI.create(resourceURI.toString().substring(0, resourceURI.toString().indexOf("/files")));
		if (!Arrays.stream(discovery.knownUrisOf("files")).toList().contains(serverURI))
			return null;

		try {
			return this.filesCache.get(serverURI, () -> {
				if (serverURI.toString().endsWith("rest")) {
					return new Pair<>(serverURI, new RestFilesClient(serverURI));
				} else {
					return new Pair<>(serverURI, new SoapFilesClient(serverURI));
				}
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public void deletedFileFromServer(URI resourceURI) {
		String serverURI = resourceURI.toString().substring(0, resourceURI.toString().indexOf("/files"));
		try {
			URI uri = new URI(serverURI);
			if (distribution.containsKey(uri))
				distribution.put(uri, distribution.get(uri) - 1);
			else
				distribution.put(uri, 0);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
