package tp1.clients.soap;

import com.sun.xml.ws.client.BindingProviderProperties;
import jakarta.xml.ws.BindingProvider;

public class SoapUtils {

	private static final int CONNECT_TIMEOUT = 10000, READ_TIMEOUT = 10000;

	public static void setTimeouts(BindingProvider port) {
		port.getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
		port.getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, READ_TIMEOUT);
	}
}
