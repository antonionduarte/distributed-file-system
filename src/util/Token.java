package util;

public class Token {

	private static String savedToken;

	private Token() {}
	
	public static void set(String t) {
		savedToken = t;
	}
	
	public static String get() {
		return savedToken == null ? "" : savedToken;
	}
	
	public static boolean matches(String t) {
		return savedToken != null && savedToken.equals( t );
	}
}
