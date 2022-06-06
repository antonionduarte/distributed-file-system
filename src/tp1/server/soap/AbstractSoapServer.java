package tp1.server.soap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jakarta.xml.ws.Endpoint;
import tp1.server.common.AbstractServer;
import util.Discovery;

import javax.net.ssl.SSLContext;

public class AbstractSoapServer extends AbstractServer {
	private static final String SERVER_BASE_URI = "https://%s:%s/soap";

	final Object implementor;

	protected AbstractSoapServer( boolean enableSoapDebug, Logger log, String service, int port, Object implementor) {
		super( log, service, port);
		this.implementor = implementor;

		if(enableSoapDebug ) {
			System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
			System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
			System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
			System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
		}
	}

	protected void start() throws IOException, NoSuchAlgorithmException {
		var ip = InetAddress.getLocalHost().getHostAddress();
		var serverURI = String.format(SERVER_BASE_URI, ip, port);

		var server = HttpsServer.create(new InetSocketAddress(ip, port), 0);

		server.setExecutor(Executors.newCachedThreadPool());
		server.setHttpsConfigurator(new HttpsConfigurator(SSLContext.getDefault()));
		server.setExecutor(Executors.newCachedThreadPool());

		var endpoint = Endpoint.create(implementor);
		endpoint.publish(server.createContext("/soap"));

		server.start();

		Discovery.getInstance().start(service, serverURI);

		Log.info(String.format("%s Soap Server ready @ %s\n", service, serverURI));
	}
}