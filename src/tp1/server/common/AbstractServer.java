package tp1.server.common;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public abstract class AbstractServer {
	final protected int port;
	final protected Logger Log;
	final protected String service;

	protected AbstractServer(Logger log, String service, int port) {
		this.service = service;
		this.port = port;
		this.Log = log;
	}

	abstract protected void start() throws IOException, NoSuchAlgorithmException;

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}
}
