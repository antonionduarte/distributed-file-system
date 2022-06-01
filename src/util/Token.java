package util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static tp1.clients.rest.RestClient.RETRY_SLEEP;

public class Token {

	private static final String DELIMITER = "_";

	private Token() {}

	static MessageDigest md;

	static {
		try {
			md = MessageDigest.getInstance("SHA256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	synchronized public static String generate(Object ...values) {
		md.reset();
		long time = System.currentTimeMillis() + RETRY_SLEEP;
		md.update((byte) (time));
		for( var o : values )
			md.update( o.toString().getBytes() );

		return String.format("%016X", new BigInteger(1, md.digest()))+DELIMITER+time;
	}

	synchronized public static boolean validate(String token, Object ...values) {
		String[] parts = token.split(DELIMITER);
		String tokenPart = parts[0];
		long time = Long.parseLong(parts[1]);

		if ( time < System.currentTimeMillis())
			return false;

		md.update((byte) (time));
		for( var o : values )
			md.update( o.toString().getBytes() );

		String generatedToken = String.format("%016X", new BigInteger(1, md.digest()));

		return generatedToken.equals(tokenPart);
	}
}
