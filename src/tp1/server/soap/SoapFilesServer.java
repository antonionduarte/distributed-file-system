package tp1.server.soap;


import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jakarta.xml.ws.Endpoint;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.server.soap.services.SoapDirectoryWebService;
import tp1.server.soap.services.SoapFilesWebService;
import util.Debug;
import util.Discovery;
import util.Secret;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoapFilesServer extends AbstractSoapServer {

	public static final int PORT = 8080;
	private static final Logger Log = Logger.getLogger(SoapFilesServer.class.getName());

	protected SoapFilesServer() {
		super(false, Log, Files.SERVICE_NAME, PORT, new SoapFilesWebService());
	}

	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.SD2122);
		Secret.set(args[0]);

		new SoapFilesServer().start();
	}
}
