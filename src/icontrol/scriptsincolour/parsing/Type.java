package icontrol.scriptsincolour.parsing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A set of enums that capture the output classifications for the parser output. 
 * The single-character ones are fixed to the punctuation they represent while
 * the empty remaining ones are paired with actual text in <code>Token</code> 
 * instances.
 * 
 * @author Brian Remedios
 */
public enum Type {
	AtSign('@'),
	Apostrophe('`'),
	Ampersand('&'),
	Colon(':'),
	Comma(','),
	Dash('-'),
	Dollar('$'),
	Equals('='),
	Hash('#'),
	Caret('^'),
	Tilde('~'),
	Percent('%'),
	Exclamation('!'),
	SingleQuote('\''),
	DoubleQuote('"'),
	Pipe('|'),
	FwdSlash('/'),
	BackSlash('\\'),
	OpenChevron('<'),
	OpenCurly('{'),
	OpenRound('('),
	OpenSquare('['),
	CloseChevron('>'),
	CloseCurly('}'),
	CloseRound(')'),
	CloseSquare(']'),
	Star('*'),
	Plus('+'),
	Period('.'),	
	SemiColon(';'),	
	Question('?'),
	Underscore('_'),

	IntNumber,
	FloatNumber,
	Keyword,
	Word,
	Quoted,		//	text bounded by single or double quotes 
	Comment,
	IPAddress,
	Whitespace;
	
	public final String letter;	// null for non-letters (more than 1 char)
	
	public static final Set<Type> Brackets = new HashSet<Type>(
			Arrays.asList(
				OpenRound, CloseRound, OpenCurly, CloseCurly,
				OpenChevron, CloseChevron, OpenSquare, CloseSquare
				)
			);
	
	private static final Map<Character, Type> TypesByChar = new HashMap<Character,Type>(singleCharTypes());
	
	public static Type forChar(char ch) { return TypesByChar.get(ch); }
	
	private Type() {
		letter = null;
	}
	
	private static Map<Character, Type> singleCharTypes() {
		
		Map<Character, Type> typesByChar = new HashMap<Character, Type>();
		
		for (Type t : values()) {
			if (t.letter != null && t.letter.length() == 1) {
				typesByChar.put(t.letter.charAt(0), t);
			}
		}
		return typesByChar;
	}
	
	private Type(char theChar) {
		letter = Character.toString(theChar);
	}
}
