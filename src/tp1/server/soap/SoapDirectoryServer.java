package tp1.server.soap;


import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jakarta.xml.ws.Endpoint;
import tp1.api.service.util.Directory;
import tp1.server.soap.services.SoapDirectoryWebService;
import util.Debug;
import util.Discovery;
import util.Secret;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoapDirectoryServer extends AbstractSoapServer {

	public static final int PORT = 8080;
	private static final Logger Log = Logger.getLogger(SoapDirectoryServer.class.getName());

	protected SoapDirectoryServer() {
		super(false, Log, Directory.SERVICE_NAME, PORT, new SoapDirectoryWebService());
	}

	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.SD2122);
		Secret.set(args[0]);

		new SoapDirectoryServer().start();
	}
}
