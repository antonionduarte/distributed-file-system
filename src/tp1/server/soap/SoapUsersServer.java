package tp1.server.soap;


import tp1.api.service.util.Users;
import tp1.server.soap.services.SoapUsersWebService;
import util.Debug;
import util.Secret;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SoapUsersServer extends AbstractSoapServer {

	public static final int PORT = 8080;
	private static final Logger Log = Logger.getLogger(SoapUsersServer.class.getName());

	protected SoapUsersServer() {
		super(false, Log, Users.SERVICE_NAME, PORT, new SoapUsersWebService());
	}

	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.SD2122);
		Secret.set(args[0]);

		new SoapFilesServer().start();
	}
}
