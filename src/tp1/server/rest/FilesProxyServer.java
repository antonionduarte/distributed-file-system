package tp1.server.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import tp1.server.rest.resources.FilesProxyResource;
import tp1.server.rest.resources.FilesResource;
import tp1.server.util.CustomLoggingFilter;
import tp1.server.util.GenericExceptionMapper;
import util.Debug;
import util.Discovery;
import util.Secret;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilesProxyServer {

	public static final int PORT = 8080;
	public static final String SERVICE = "files";
	private static final Logger Log = Logger.getLogger(FilesProxyServer.class.getName());
	private static final String SERVER_URI_FMT = "https://%s:%s/rest";

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static void main(String[] args) {
		try {
			Debug.setLogLevel(Level.INFO, Debug.SD2122);

			Secret.set(args[1]);

			ResourceConfig config = new ResourceConfig();
			config.register(new FilesProxyResource(Boolean.parseBoolean(args[0]), args[1], args[2], args[3]));
			config.register(CustomLoggingFilter.class);
			config.register(GenericExceptionMapper.class);

			String ip = InetAddress.getLocalHost().getHostAddress();
			String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

			Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

			Discovery discovery = Discovery.getInstance();
			discovery.start(SERVICE, serverURI);

			// More code can be executed here...
		} catch (Exception e) {
			Log.severe(e.getMessage());
		}
	}
}
