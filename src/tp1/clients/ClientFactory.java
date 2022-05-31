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
import util.Discovery;
import util.Pair;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ClientFactory {

	private static final int STORE_IN_HOW_MANY = 2;

	private static final Discovery discovery = Discovery.getInstance();

	private static final int CACHE_DURATION = 10; // 10 seconds for now
	private static ClientFactory instance;
	private final Cache<String, Pair<String, Users>> usersCache;
	private final Cache<String, Pair<String, Files>> filesCache;
	private final Cache<String, Pair<String, Directory>> directoryCache;
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

	public Pair<String, Users> getUsersClient() {
		var serverURI = discovery.knownUrisOf("users").get(0);

		try {
			return this.usersCache.get(serverURI.toString(), () -> {
				if (serverURI.toString().endsWith("rest")) {
					return new Pair<>(serverURI.toString(), new RestUsersClient(serverURI));
				} else {
					return new Pair<>(serverURI.toString(), new SoapUsersClient(serverURI));
				}
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public Pair<String, Directory> getDirectoryClient() {
		var serverURI = discovery.knownUrisOf("directory").get(0);

		try {
			return this.directoryCache.get(serverURI.toString(), () -> {
				if (serverURI.toString().endsWith("rest")) {
					return new Pair<>(serverURI.toString(), new RestDirectoryClient(serverURI));
				} else {
					return new Pair<>(serverURI.toString(), new SoapDirectoryClient(serverURI));
				}
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<Pair<String, Files>> getFilesClients() {
		var serverURIs = discovery.knownUrisOf("files");

		for (URI serverURI : serverURIs) {
			distribution.putIfAbsent(serverURI, 0);
		}

		var urisFiles = minFiles(serverURIs);

		Set<Pair<String, Files>> clients = new HashSet<>();
		for (URI uri : urisFiles) {
			distribution.put(uri, distribution.get(uri) + 1);

			try {
				clients.add(this.filesCache.get(urisFiles.toString(), () -> {
					if (uri.toString().endsWith("rest")) {
						return new Pair<>(uri.toString(), new RestFilesClient(uri));
					} else {
						return new Pair<>(uri.toString(), new SoapFilesClient(uri));
					}
				}));
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		return clients;
	}

	//returns STORE_IN_HOW_MANY servers with the lowest storage
	private Set<URI> minFiles(List<URI> serverURIs) {
		Set<URI> uris = new HashSet<>(STORE_IN_HOW_MANY);

		for (int i = 0; i < STORE_IN_HOW_MANY; i++) {
			Map.Entry<URI, Integer> min = null;
			for (Map.Entry<URI, Integer> entry : distribution.entrySet()) {

				if (serverURIs.contains(entry.getKey()) && (min == null || min.getValue() > entry.getValue()) && !uris.contains(entry.getKey())) {
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

	public Pair<String, Files> getFilesClient(String resourceURI) {
		String serverURI = resourceURI.substring(0, resourceURI.indexOf("/files"));
		if (!discovery.knownUrisOf("files").contains(URI.create(serverURI)))
			return null;

		try {
			return this.filesCache.get(serverURI, () -> {
				if (serverURI.endsWith("rest")) {
					return new Pair<>(serverURI, new RestFilesClient(new URI(serverURI)));
				} else {
					return new Pair<>(serverURI, new SoapFilesClient(new URI(serverURI)));
				}
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public void deletedFileFromServer(String resourceURI) {
		String serverURI = resourceURI.substring(0, resourceURI.indexOf("/files"));
		try {
			URI uri = new URI(serverURI);
			distribution.put(uri, distribution.get(uri) - 1);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
