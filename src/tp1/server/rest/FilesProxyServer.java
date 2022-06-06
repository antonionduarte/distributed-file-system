package tp1.server.rest;

import org.glassfish.jersey.server.ResourceConfig;
import tp1.api.service.util.Files;
import tp1.server.rest.resources.FilesProxyResource;
import tp1.server.util.GenericExceptionMapper;
import util.Debug;
import util.Secret;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FilesProxyServer extends AbstractRestServer {

	public static final int PORT = 8080;

	private static final Logger Log = Logger.getLogger(FilesProxyServer.class.getName());
	private static boolean deleteAll;
	private static String apiKey;
	private static String apiSecret;
	private static String accessKey;


	FilesProxyServer() {
		super(Log, Files.SERVICE_NAME, PORT);
	}

	@Override
	void registerResources(ResourceConfig config) {
		config.register( new FilesProxyResource(deleteAll, apiKey, apiSecret, accessKey));
		config.register( GenericExceptionMapper.class );
//		config.register( CustomLoggingFilter.class);
	}

	public static void main(String[] args) throws Exception {

		Debug.setLogLevel( Level.INFO, Debug.SD2122);

		deleteAll = Boolean.parseBoolean(args[0]);
		apiKey = args[2];
		apiSecret = args[3];
		accessKey = args[4];
		
		Secret.set(args[1]);

		new FilesProxyServer().start();
	}
}
