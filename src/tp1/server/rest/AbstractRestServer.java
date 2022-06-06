package tp1.server.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import tp1.server.common.AbstractServer;
import util.Discovery;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public abstract class AbstractRestServer extends AbstractServer {

	protected static final String SERVER_BASE_URI = "https://%s:%s/rest";

	protected AbstractRestServer(Logger log, String service, int port) {
		super(log, service, port);
	}


	protected void start() throws UnknownHostException, NoSuchAlgorithmException {
		String ip = InetAddress.getLocalHost().getHostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, port);

		ResourceConfig config = new ResourceConfig();

		registerResources( config );

		JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config, SSLContext.getDefault());

		Log.info(String.format("%s Server ready @ %s\n",  service, serverURI));

		Discovery.getInstance().start(service, serverURI);
	}

	abstract void registerResources( ResourceConfig config );
}
