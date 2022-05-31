package util;

public class Secret {

	private static String secret;

	private Secret() {}
	
	public static void set(String s) {
		secret = s;
	}
	
	public static String get() {
		return secret == null ? "" : secret;
	}
}
