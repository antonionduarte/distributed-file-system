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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ClientFactory {

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
		var serverURI = discovery.knownUrisOf("directory").get(0); // use discovery to find an uri of the Users service

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

	public Pair<String, Files> getFilesClient() {
		var serverURIs = discovery.knownUrisOf("files"); // use discovery to find an uri of the Users service

		for (URI serverURI : serverURIs) {
			distribution.putIfAbsent(serverURI, 0);
		}

		var serverURI = minFiles(serverURIs);

		distribution.put(serverURI, distribution.get(serverURI) + 1);

		try {
			return this.filesCache.get(serverURI.toString(), () -> {
				if (serverURI.toString().endsWith("rest")) {
					return new Pair<>(serverURI.toString(), new RestFilesClient(serverURI));
				} else {
					return new Pair<>(serverURI.toString(), new SoapFilesClient(serverURI));
				}
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private URI minFiles(List<URI> serverURIs) {
		Map.Entry<URI, Integer> min = null;
		for (Map.Entry<URI, Integer> entry : distribution.entrySet()) {

			if (serverURIs.contains(entry.getKey()) && (min == null || min.getValue() > entry.getValue())) {
				min = entry;
			}
		}

		assert min != null;
		return min.getKey();
	}

	public Pair<String, Files> getFilesClient(String resourceURI) {
		String serverURI = resourceURI.substring(0, resourceURI.indexOf("/files"));
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
