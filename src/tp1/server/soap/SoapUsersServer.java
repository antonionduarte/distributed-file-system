package tp1.server.soap;


import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jakarta.xml.ws.Endpoint;
import tp1.server.soap.services.SoapUsersWebService;
import util.Discovery;
import util.Secret;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoapUsersServer {

	public static final int PORT = 8080;
	public static final String SERVICE_NAME = "users";
	private static final Logger Log = Logger.getLogger(SoapUsersServer.class.getName());
	public static final String SERVER_BASE_URI = "https://%s:%s/soap";

	public static void main(String[] args) throws Exception {
		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

		Log.setLevel(Level.INFO);

		String ip = InetAddress.getLocalHost().getHostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

		var server = HttpsServer.create(new InetSocketAddress(ip, PORT), 0);

		server.setExecutor(Executors.newCachedThreadPool());
		server.setHttpsConfigurator(new HttpsConfigurator(SSLContext.getDefault()));

		Secret.set(args[0]);

		var endpoint = Endpoint.create(SoapUsersWebService.class);
		endpoint.publish(server.createContext("/soap"));

		server.start();

		Discovery discovery = Discovery.getInstance();
		discovery.start(SERVICE_NAME, serverURI);

		Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME, serverURI));
	}
}
