package tp1.clients.rest;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import tp1.clients.InsecureHostnameVerifier;

import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class RestClient {
	public static final int RETRY_SLEEP = 1000;
	protected static final int READ_TIMEOUT = 10000;
	protected static final int CONNECT_TIMEOUT = 10000;
	protected static final int MAX_RETRIES = 3;
	private static final Logger Log = Logger.getLogger(RestClient.class.getName());
	final URI serverURI;
	final Client client;
	final ClientConfig config;

	RestClient(URI serverURI) {
		this.serverURI = serverURI;
		this.config = new ClientConfig();

		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);

		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
	}

	protected <T> T reTry(Supplier<T> func) {
		for (int i = 0; i < MAX_RETRIES; i++)
			try {
				return func.get();
			} catch (ProcessingException x) {
				Log.fine("ProcessingException: " + x.getMessage());
				sleep();
			} catch (Exception x) {
				Log.fine("Exception: " + x.getMessage());
				x.printStackTrace();
				break;
			}
		return null;
	}

	private void sleep() {
		try {
			Thread.sleep(RestClient.RETRY_SLEEP);
		} catch (InterruptedException x) { // nothing to do...
		}
	}
}
