package tp1.server.rest;

import org.glassfish.jersey.server.ResourceConfig;
import tp1.api.service.util.Directory;
import tp1.server.rest.resources.ReplicatedDirectoryResource;
import tp1.server.util.CustomLoggingFilter;
import tp1.server.util.GenericExceptionMapper;
import util.Debug;
import util.Secret;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplicatedDirectoryServer extends AbstractRestServer{

	public static final int PORT = 8080;

	private static final Logger Log = Logger.getLogger(ReplicatedDirectoryServer.class.getName());

	ReplicatedDirectoryServer() {
		super(Log, Directory.SERVICE_NAME, PORT);
	}

	@Override
	void registerResources(ResourceConfig config) {
		config.register( ReplicatedDirectoryResource.class);
		config.register( GenericExceptionMapper.class );
		config.register( CustomLoggingFilter.class);
	}

	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.SD2122);

		Secret.set(args[0]);

		new ReplicatedDirectoryServer().start();
	}
}
