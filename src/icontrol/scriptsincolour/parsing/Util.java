package icontrol.scriptsincolour.parsing;

import java.util.List;

/**
 * Some general purpose methods useful for parsing and manipulating text.
 *
 * @author Brian Remedios
 */
public class Util {

	private Util() { }
	
	public static boolean isWhitespace(String text) {
		
		for (int i=0; i<text.length(); i++) {
			if (Character.isWhitespace(text.charAt(i))) continue;
			return false;
		}
		return true;
	}
	
	public static boolean isInteger(String text) {
		
		for (int i=0; i<text.length(); i++) {
			if (Character.isDigit(text.charAt(i))) continue;
			return false;
		}
		return true;
	}

	public static String asString(List<Token> parts) {
	
		if (parts.isEmpty()) return "";
		if (parts.size() == 1) return parts.get(0).text;
	
		StringBuilder sb = new StringBuilder();
		for (Token t : parts) sb.append(t.text);
		return sb.toString();		
	}
}
