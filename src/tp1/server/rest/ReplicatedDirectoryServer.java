package tp1.server.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import tp1.server.rest.resources.ReplicatedDirectoryResource;
import tp1.server.util.CustomLoggingFilter;
import tp1.server.util.GenericExceptionMapper;
import util.Debug;
import util.Discovery;
import util.Secret;
import util.kafka.sync.SyncPoint;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplicatedDirectoryServer {

	public static final int PORT = 8080;
	public static final String SERVICE = "directory";

	private static final Logger Log = Logger.getLogger(ReplicatedDirectoryServer.class.getName());
	private static final String SERVER_URI_FMT = "https://%s:%s/rest";

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static void main(String[] args) {
		try {
			Debug.setLogLevel(Level.INFO, Debug.SD2122);

			Secret.set(args[0]);
			SyncPoint<String> syncPoint = new SyncPoint<>();

			ResourceConfig config = new ResourceConfig();
			config.register(new ReplicatedDirectoryResource(syncPoint));
			config.register(CustomLoggingFilter.class);
			config.register(GenericExceptionMapper.class);

			String ip = InetAddress.getLocalHost().getHostAddress();
			String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

			Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

			Discovery discovery = Discovery.getInstance();
			discovery.start(SERVICE, serverURI);
		} catch (Exception e) {
			Log.severe(e.getMessage());
		}
	}

}
