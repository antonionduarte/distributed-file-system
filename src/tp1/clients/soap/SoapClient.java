package tp1.clients.soap;

import jakarta.ws.rs.ProcessingException;
import tp1.clients.rest.RestClient;

import java.net.URI;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class SoapClient {
	private static final Logger Log = Logger.getLogger(SoapClient.class.getName());
	
	private static final int MAX_RETRIES = 3;
	private static final int RETRY_SLEEP = 1000;

	final URI serverURI;

	public SoapClient(URI serverURI) {
		this.serverURI = serverURI;
	}

	protected <T> T reTry(Supplier<T> func) {
		for (int i = 0; i < MAX_RETRIES; i++)
			try {
				return func.get();
			} catch (ProcessingException x) {
				Log.fine("ProcessingException: " + x.getMessage());
				sleep(RETRY_SLEEP);
			} catch (Exception x) {
				Log.fine("Exception: " + x.getMessage());
				x.printStackTrace();
				break;
			}
		return null;
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException x) { // nothing to do...
		}
	}
}
