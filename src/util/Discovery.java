package util;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

/**
 * Performs service discovery. Used by servers to announce themselves, and clients
 * to discover services on demand.
 *
 * @author smduarte
 *
 * <p>A class to perform service discovery, based on periodic service contact endpoint
 * announcements over multicast communication.</p>
 *
 * <p>Servers announce their *name* and contact *uri* at regular intervals. The server actively
 * collects received announcements.</p>
 *
 * <p>Service announcements have the following format:</p>
 *
 * <p>&lt;service-name-string&gt;&lt;delimiter-char&gt;&lt;service-uri-string&gt;</p>
 */
public class Discovery {
	private static Discovery instance = null;

	private static final Logger Log = Logger.getLogger(Discovery.class.getName());

	static {
		// addresses some multicast issues on some TCP/IP stacks
		System.setProperty("java.net.preferIPv4Stack", "true");
		// summarizes the logging format
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}


	// The pre-agreed multicast endpoint assigned to perform discovery.
	static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("227.227.227.227", 2277);
	static final int DISCOVERY_PERIOD = 1000;
	static final int DISCOVERY_RESET = 5000;
	static final int DISCOVERY_TIMEOUT = 5000;

	// Used separate the two fields that make up a service announcement.
	private static final String DELIMITER = "\t";

	private static Map<String, Set<URI>> services = new ConcurrentHashMap<>();


	private Discovery() { }

	public static Discovery getInstance() {
		if (instance == null) {
			instance = new Discovery();
		}
		return instance;
	}

	/**
	 * Continuously announces a service given its name and uri
	 *
	 * @param serviceName the composite service name: <domain:service>
	 * @param serviceURI - the uri of the service
	 */
	public void announce(String serviceName, String serviceURI) {
		Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s\n",
				DISCOVERY_ADDR, serviceName, serviceURI));

		var pktBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();

		DatagramPacket pkt = new DatagramPacket(pktBytes, pktBytes.length, DISCOVERY_ADDR);
		// start thread to send periodic announcements
		new Thread(() -> {
			try (var ds = new DatagramSocket()) {
				for (;;) {
					try {
						Thread.sleep(DISCOVERY_PERIOD);
						ds.send(pkt);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}


	public void listener() {
		Log.info(String.format("Starting discovery on multicast group: %s, port: %d\n",
				DISCOVERY_ADDR.getAddress(), DISCOVERY_ADDR.getPort()));

		final int MAX_DATAGRAM_SIZE = 65536;
		var pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);

		new Thread(() -> {
			try (var ms = new MulticastSocket(DISCOVERY_ADDR.getPort())) {
				joinGroupInAllInterfaces(ms);
				for(;;) {
					try {
						pkt.setLength(MAX_DATAGRAM_SIZE);
						ms.receive(pkt);

						var msg = new String(pkt.getData(), 0, pkt.getLength());
						/*System.out.printf( "FROM %s (%s) : %s\n", pkt.getAddress().getCanonicalHostName(),
								pkt.getAddress().getHostAddress(), msg);*/
						var tokens = msg.split(DELIMITER);

						if (tokens.length == 2) {
							String service = tokens[0];
							URI newUri = URI.create(tokens[1]);
							Set<URI> uris;

							uris = services.computeIfAbsent(service, k -> new CopyOnWriteArraySet<>());
							uris.add(newUri);

							services.put(service, uris);
						}

					} catch (IOException e) {
						e.printStackTrace();
						try {
							Thread.sleep(DISCOVERY_PERIOD);
						} catch (InterruptedException e1) {
							// do nothing
						}
						Log.finest("Still listening...");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private void resetPeriodically() {
		new Thread(() -> {
			for(;;) {
				try {
					services = new ConcurrentHashMap<>();
					Thread.sleep(DISCOVERY_RESET);
				} catch (InterruptedException e) {
					// nothing
				}
			}
		}).start();
	}

	/**
	 * Returns the known servers for a service.
	 *
	 * @param  serviceName the name of the service being discovered
	 * @return an array of URI with the service instances discovered.
	 *
	 */
	public URI[] knownUrisOf(String serviceName) {
		URI[] uris = retrieveUris(serviceName);
		int replies = uris.length;

		try {
			while (replies == 0) {
				Thread.sleep(DISCOVERY_PERIOD);
				uris = retrieveUris(serviceName);
				replies = uris.length;
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		return uris;
	}


	private URI[] retrieveUris(String serviceName) {
		if (!services.containsKey(serviceName))
			return new URI[0];

		else {
			Set<URI> uris = services.get(serviceName);
			return uris.toArray(new URI[0]);
		}
	}


	private void joinGroupInAllInterfaces(MulticastSocket ms) throws SocketException {
		Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
		while (ifs.hasMoreElements()) {
			NetworkInterface xface = ifs.nextElement();
			try {
				ms.joinGroup(DISCOVERY_ADDR, xface);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}

	public void start(String serviceName, String serviceURI) {
		announce(serviceName, serviceURI);
		listener();
		resetPeriodically();
	}

}