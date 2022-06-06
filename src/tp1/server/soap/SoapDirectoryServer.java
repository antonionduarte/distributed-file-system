package tp1.server.soap;


import tp1.api.service.util.Directory;
import tp1.server.soap.services.SoapDirectoryWebService;
import util.Debug;
import util.Secret;

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
