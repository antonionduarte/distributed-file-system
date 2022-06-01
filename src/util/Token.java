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

	synchronized public static String generate(Object ...values) {
		md.reset();
		md.update((byte) (System.currentTimeMillis() / 1000));
		for( var o : values )
			md.update( o.toString().getBytes() );

		return String.format("%016X", new BigInteger(1, md.digest()));
	}
}
