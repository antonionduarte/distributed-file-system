package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Token {

	private Token() {}

	public static String generate(String secret, String... args) {

		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			for (String arg : args) {
				os.write(arg.getBytes());
			}
			long currentTimeInSeconds = System.currentTimeMillis()/1000;
			os.write((byte) currentTimeInSeconds);
			os.write(secret.getBytes());

			return SHAsum(os.toByteArray());
		} catch (IOException | NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static String SHAsum(byte[] convertMe) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		return byteArray2Hex(md.digest(convertMe));
	}

	private static String byteArray2Hex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}
}
