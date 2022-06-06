package tp1.clients.soap;

import com.sun.xml.ws.client.BindingProviderProperties;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import tp1.clients.InsecureHostnameVerifier;

import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class SoapClient {
	private static final Logger Log = Logger.getLogger(SoapClient.class.getName());

	private static final int CONNECT_TIMEOUT = 10000, READ_TIMEOUT = 10000;

	private static final int MAX_RETRIES = 3;
	private static final int RETRY_SLEEP = 1000;

	final URI serverURI;

	public SoapClient(URI serverURI) {
		this.serverURI = serverURI;

		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
	}

	protected static void setTimeouts(BindingProvider port) {
		port.getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
		port.getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, READ_TIMEOUT);
	}

	protected <T> T reTry(Supplier<T> func) {
		for (int i = 0; i < MAX_RETRIES; i++)
			try {
				return func.get();
			} catch (WebServiceException x) {
				Log.fine("WebServiceException: " + x.getMessage());
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
			Thread.sleep(SoapClient.RETRY_SLEEP);
		} catch (InterruptedException x) { // nothing to do...
		}
	}
}
