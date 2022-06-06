package tp1.server.soap;


import tp1.api.service.util.Files;
import tp1.server.soap.services.SoapFilesWebService;
import util.Debug;
import util.Secret;

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
