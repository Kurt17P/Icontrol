package icontrol.scriptsincolour.parsing;
/**
 * An immutable simple tuple that links a chunk of parsed text to the 
 * <code>Type</code> that identifies it.
 *
 * @author Brian Remedios
 */
public class Token {

	public final String text;
	public final Type type;
	
	public Token(Type theType, String theText) {
		type = theType;
		text = theText;
	}

	public String toString() {
		return type + " " + text;
	}
}
