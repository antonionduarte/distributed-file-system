package util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Token {

	private Token() {}

	static MessageDigest md;

	static {
		try {
			md = MessageDigest.getInstance("SHA256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	synchronized private static byte[] digest(byte[] data) {
		md.reset();
		md.update(data);
		return md.digest();
	}

	public static String generate(byte[] data) {
		return String.format("%016X", new BigInteger(1,digest(data)));
	}

	public static String generate(String data) {
		return generate( data.getBytes() );
	}

	synchronized public static String generate(Object ...values) {
		md.reset();
		for( var o : values )
			md.update( o.toString().getBytes() );

		return String.format("%016X", new BigInteger(1, md.digest()));
	}
}
