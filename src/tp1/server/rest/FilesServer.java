package tp1.server.rest;

import org.glassfish.jersey.server.ResourceConfig;
import tp1.api.service.util.Files;
import tp1.server.rest.resources.FilesResource;
import tp1.server.util.GenericExceptionMapper;
import util.Debug;
import util.Secret;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FilesServer extends  AbstractRestServer {

	public static final int PORT = 8080;

	private static final Logger Log = Logger.getLogger(FilesServer.class.getName());


	FilesServer() {
		super(Log, Files.SERVICE_NAME, PORT);
	}

	@Override
	void registerResources(ResourceConfig config) {
		config.register( FilesResource.class );
		config.register( GenericExceptionMapper.class );
//		config.register( CustomLoggingFilter.class);
	}

	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.SD2122);

		Secret.set(args[0]);

		new FilesServer().start();
	}
}
