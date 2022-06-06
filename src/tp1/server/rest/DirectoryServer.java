package tp1.server.rest;

import org.glassfish.jersey.server.ResourceConfig;
import tp1.api.service.util.Directory;
import tp1.server.rest.resources.DirectoryResource;
import tp1.server.util.GenericExceptionMapper;
import util.Debug;
import util.Secret;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectoryServer extends AbstractRestServer {

	public static final int PORT = 8080;

	private static final Logger Log = Logger.getLogger(DirectoryServer.class.getName());

	DirectoryServer() {
		super(Log, Directory.SERVICE_NAME, PORT);
	}

	@Override
	void registerResources(ResourceConfig config) {
		config.register( DirectoryResource.class );
		config.register( GenericExceptionMapper.class );
//		config.register( CustomLoggingFilter.class);
	}

	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.SD2122);

		Secret.set(args[0]);

		new DirectoryServer().start();
	}

}
