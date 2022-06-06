package tp1.server.rest;

import org.glassfish.jersey.server.ResourceConfig;
import tp1.api.service.util.Users;
import tp1.server.rest.resources.UsersResource;
import tp1.server.util.GenericExceptionMapper;
import util.Debug;
import util.Secret;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UsersServer extends AbstractRestServer {

	public static final int PORT = 8080;

	private static final Logger Log = Logger.getLogger(UsersServer.class.getName());

	UsersServer() {
		super( Log, Users.SERVICE_NAME, PORT);
	}


	@Override
	void registerResources(ResourceConfig config) {
		config.register( UsersResource.class );
		config.register( GenericExceptionMapper.class);
	}


	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.SD2122);

		Secret.set(args[0]);

		new UsersServer().start();
	}
}
