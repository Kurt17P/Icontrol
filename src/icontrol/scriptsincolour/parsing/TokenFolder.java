package icontrol.scriptsincolour.parsing;

import static icontrol.scriptsincolour.parsing.Type.BackSlash;
import static icontrol.scriptsincolour.parsing.Type.Colon;
import static icontrol.scriptsincolour.parsing.Type.Comment;
import static icontrol.scriptsincolour.parsing.Type.DoubleQuote;
import static icontrol.scriptsincolour.parsing.Type.FloatNumber;
import static icontrol.scriptsincolour.parsing.Type.FwdSlash;
import static icontrol.scriptsincolour.parsing.Type.IPAddress;
import static icontrol.scriptsincolour.parsing.Type.IntNumber;
import static icontrol.scriptsincolour.parsing.Type.Keyword;
import static icontrol.scriptsincolour.parsing.Type.Period;
import static icontrol.scriptsincolour.parsing.Type.Percent;
import static icontrol.scriptsincolour.parsing.Type.Pipe;
import static icontrol.scriptsincolour.parsing.Type.SingleQuote;
import static icontrol.scriptsincolour.parsing.Type.Underscore;
import static icontrol.scriptsincolour.parsing.Type.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
/**
 *  The token folder accepts a sequence of primitive tokens, recognizes higher-level items
 *  within them and emits the higher-level ones in lieu of the primitives. 
 * 
 * I.e.: 
 *   IntNumber + Period + IntNumber  ->  FloatNumber
 *   
 *  Without any input args, it recognizes negative and floating point numbers including those 
 *  in scientific notation, quoted strings with possible escaped quotes, compound identifiers 
 *  joined with underscores, IP addresses.
 *  
 *  If you supply a set of your favourite keywords it will identify them as well (outside of
 *  any comments)
 *  
 * @author Brian Remedios
 */
public class TokenFolder {

	private LinkedList<Token> tokens;	// input
	private List<Token> folded;			// output
	
	private final Set<String> keywords;	// TODO distinguish between iControl and Python keywords
	
	/**
	 * 
	 */
	public TokenFolder() {	
		this(Collections.<String>emptySet());
	}

	/**
	 * 
	 * @param theKeywords
	 */
	public TokenFolder(Set<String> theKeywords) {
		keywords = theKeywords;
	}
	
	/**
	 * Return the type of the next token or null if the queue is empty.
	 * 
	 * @return type
	 */
	private Type nextType() {
		if (tokens.isEmpty()) return null;
		return tokens.peek().type;
	}

	/**
	 * Return whether the text at the next token
	 * matches any of the choices.
	 *
	 * @param choices
	 *
	 * @return boolean
	 */
	private boolean nextTextIsAny(String... choices) {
		
		if (tokens.isEmpty()) return false;
		
		String txt = tokens.peek().text;
		for (String choice : choices) {
			if (choice.equals(txt)) return true;
		}
		return false;
	}

	/**
	 * Return whether the next tokens isn't one that matches the specified type.
	 * 
	 * @param type
	 * @return boolean
	 */
	private boolean nextIsNot(Type type) { return nextType() != type; }

	/**
	 * Return whether the next token denotes
	 * an integer number.
	 * 
	 * @return boolean
	 */
	private boolean nextIsNumber() { return nextType() == Type.IntNumber; }

	/**
	 * Return whether the next token denotes
	 * a whitespace.
	 * 
	 * @return boolean
	 */
	private boolean nextIsWhitespace() { return nextType() == Type.Whitespace; }

	/**
	 * Return the token at the specified index or null
	 * if its out of range.
	 *
	 * @param index
	 * @return
	 */
	private Token tokenAt(int index) {
		if (index >= tokens.size()) return null;
		return tokens.get(index);
	}
	
	/**
	 * Return the type of the token at the specified index or
	 * null if the index is invalid.
	 * 
	 * @param index
	 * 
	 * @return Type
	 */
	private Type typeAt(int index) {
		Token t = tokenAt(index);
		return t != null ? t.type : null;
	}

	/**
	 * Return whether the text (if any) held by the token at the 
	 * specified index matches any of the choices.
	 *
	 * @param index
	 * @param choices
	 *
	 * @return boolean
	 */
	private boolean textAtIsAny(int index, String... choices) {

		Token t = tokenAt(index);
		if (t == null) return false;

		for (String choice : choices) {
			if (choice.equals(t.text)) return true;
		}
		return false;
	}

	private boolean previousIs(Type type, int negOffset) {
		
		int pos = folded.size() - negOffset;
		if (pos < 0) return false;
		
		Type t = folded.get(pos).type;
		return t == type;
	}
	
	/**
	 * Period followed by an integer
	 * 
	 */
	private boolean nextIsFractionPart() {

		if (tokens.isEmpty()) return false;

		// check to see if item before the int was a period
		if (previousIs(Period, 1)) return false;
		
		if (nextType() != Period) return false;
		
		Type second = typeAt(1);
		if (second == IntNumber) {
			// check for n.n.n  (like an IP address)
			if (typeAt(2) == Period && typeAt(3) == IntNumber) return false;
			return true;
		}
	
		return false;
	}

	/**
	 * Return the number of following tokens that represent scientific notation.
	 * 
	 * I.e. :  nne+nn, nne-nn, nnE+nn, nnE-nn, nnenn, nnEnn
	 */
	private int nextExpParts() {

		if (tokens.isEmpty()) return 0;

		if (!nextTextIsAny("e", "E")) return 0;
		
		if (typeAt(1) == IntNumber) return 1;
		if (textAtIsAny(1, "-", "+") && typeAt(2) == IntNumber) return 2;
		
		return 0;		
	}

	/**
	 * Return whether the token at the specified position is an
	 * integer one and that its value is within the min/max limits.
	 * 
	 * @param pos
	 * @param min
	 * @param max
	 * @return boolean
	 */
	private boolean isIntWithin(int pos, int min, int max) {
		
		if (tokens.isEmpty()) return false;
		Token t = tokenAt(pos);
		if (t == null || t.type != IntNumber) return false;
		
		int num = Integer.parseInt(t.text);
		return min <= num && num <= max;
	}
	
	/**
	 * Return whether the next set of tokens could represent
	 * an IPAddress (v4)
	 * 
	 * nn.nn.nn.nn  where nn >= 0 && nn < 256 
	 */
	private boolean nextIsIPAddress() {

		if (tokens.isEmpty()) return false;

		return
			typeAt(0)==Period && isIntWithin(1, 0,255) && 
			typeAt(2)==Period && isIntWithin(3, 0,255) && 
			typeAt(4)==Period && isIntWithin(5, 0,255);
	}

	/**
	 * Checking for a possible ':nnnnn'  where nnnnn is positive and less than 64K
	 * 
	 * @return boolean
	 */
	private boolean nextIsPortNum() {
		
		if (tokens.isEmpty()) return false;		
		return typeAt(0)==Colon && isIntWithin(1, 0, 65535); 
	}

	/**
	 * Check for underscore followed by word or integer
	 */
	private boolean nextIsWordPart() {

		Type typ = nextType();
		if (typ == null) return false;
		
		switch (typ) {
			case Word:
			case IntNumber :
			case Underscore: return true;			
			default : return false;
			}
	}

	private String buildIdAfter(String prefix) {

		StringBuilder sb= new StringBuilder(prefix);
		while (nextIsWordPart()) {
			sb.append(tokens.remove().text);
		}
		return sb.toString();
	}

	/**
	 * Pull the text from the next few tokens up to the index
	 * and return them as a single string.
	 *
	 * @param index
	 * 
	 * @return String
	 */
	private String collectTextTo(int index) {

		if (index >= tokens.size()) return null;
		if (index == 0) return tokens.remove().text;

		StringBuilder sb = new StringBuilder();
		for (int i=0; i<=index; i++) {
			sb.append(tokens.remove().text);
		}
		return sb.toString();
	}

	/**
	 * Pull the text from the next tokens up until the
	 * specified endType is encountered. If the endType
	 * is found preceeded by a backslash then we'll 
	 * ignore it as this convention denotes an escape
	 * sequence.
	 *
	 * If the terminating endType isn't found then we'll
	 * just pass the tokens through untreated.
	 * 
	 * @param endType
	 */
	private void collectQuotedTo(Type endType) {

		List<Token> parts = new ArrayList<Token>();
		
		Token tok;
		boolean inEsc = false;
		
		while (!tokens.isEmpty() && (inEsc || nextIsNot(endType))) {
			tok = tokens.remove();
			if (inEsc) {	// already in escape mode?
				parts.add(tok);
				inEsc = false;
				continue;
			} else {
				inEsc = tok.type == BackSlash;
			}
			
			if (!inEsc) {
				parts.add(tok);			
			}
		}		
		
		if (nextType() == endType) {
			folded.add(new Token(Type.Quoted, Util.asString(parts)));
			folded.add(tokens.remove());	// closing quote
			return;
		}
		for (Token t : parts) {	// can't make a string, :(
			folded.add(t);
		}
	}

	/**
	 * Collect the text of all the remaining tokens and return
	 * it as a single String.
	 *
	 * @return String
	 */
	private String collectToEnd() {

		StringBuilder sb = new StringBuilder();
		while (!tokens.isEmpty()) sb.append(tokens.remove().text);
		return sb.toString();
	}

	/**
	 * Return whether the next token type matches the first argument
	 * type while any of the possible nexts match their as well.
	 *
	 * @param first
	 * @param nexts
	 * 
	 * @return boolean
	 */
	private boolean contentsStartWith(Type first, Type... nexts) {

		if (folded.isEmpty()) return false;
		if (folded.get(0).type != first) return false;

		if (nexts.length == 0) return true;

		for (int i=1; i<folded.size(); i++) {
			if (folded.get(i).type != nexts[i-1]) return false;
		}
		return true;
	}
	
	/**
	 * Accept a sequence of primitive tokens and look for any that denote
	 * higher-level items and return them in their place.
	 * 
	 * @param tokenList
	 *
	 * @return List<Token>
	 */
	public List<Token> fold(List<Token> tokenList) {

		if (tokenList.size() < 2) return tokenList;

		tokens = new LinkedList<Token>(tokenList);
		folded = new ArrayList<Token>(tokens.size());

		Token current;
		while (!tokens.isEmpty()) {
			current = tokens.remove();						
			switch (current.type) {
			case Dash: {
				if (nextIsNumber()) {
					String negNum = "-" + tokens.remove().text;
					if (nextIsWhitespace()) {
						folded.add(new Token(IntNumber, negNum));
						continue;
					} 
					if (nextIsFractionPart()) {	
						negNum += collectTextTo(1);
						int expParts = nextExpParts();
						if (expParts > 0) {
							negNum += collectTextTo(expParts);
						} 
						folded.add(new Token(FloatNumber, negNum));
						continue;
						}
					int expParts = nextExpParts();
					if (expParts > 0) {
						negNum += collectTextTo(expParts);
						folded.add(new Token(IntNumber, negNum));
						continue;
					}
					folded.add(new Token(IntNumber, negNum));
				}
				break;
			}
			case IntNumber: {
				if (nextIsFractionPart()) {	// nnn.nnn ?
					String flt = current.text + collectTextTo(1);
					int expParts = nextExpParts();
					if (expParts > 0) {
						flt += collectTextTo(expParts);
						}
					folded.add(new Token(FloatNumber, flt));
					continue;
					}
				int expParts = nextExpParts();
				if (expParts > 0) {
					String num = current.text + collectTextTo(expParts);
					folded.add(new Token(IntNumber, num));
					continue;
					}
				if (nextIsIPAddress()) {
					String num = current.text + collectTextTo(5);
					if (nextIsPortNum()) {
						num += collectTextTo(1);
					}
					folded.add(new Token(IPAddress, num));
					continue;
					}
				folded.add(current);	// just an int
				break;
			}
			case SingleQuote: {
				folded.add(current);	// open quote
				collectQuotedTo(SingleQuote);
				break;
			}
			case DoubleQuote: {
				folded.add(current);	// open quote
				collectQuotedTo(DoubleQuote);
				break;
			}
			
			// three different comment tags(!):  %%, //, and #
			// TODO pull these out and make them parametric

			case FwdSlash: {
				if (nextType() == FwdSlash) {
					folded.add(new Token(Comment, "/" + collectToEnd()));
					break;
				} else {
					folded.add(current);
					break;
				}
			}
			case Percent: {
				if (nextType() == Percent) {
					folded.add(new Token(Comment, "%" + collectToEnd()));
					break;
				} else {
					folded.add(current);
					break;
				}
			}
			case Hash: {
				if (contentsStartWith(Pipe)) {	// first char is a '|'  ?				
					folded.add(new Token(Comment, "#" + collectToEnd()));
					break;
				}
			}
			
			
			case Word: {
				Type next = nextType();
				if (next == Underscore || next == IntNumber) {
					String identifier = buildIdAfter(current.text);
					folded.add(new Token(Word, identifier));
					continue;
				}	// fall through, add it
				if (keywords.contains(current.text)) {
					folded.add(new Token(Keyword, current.text));
					continue;
				}
			}
			default: folded.add(current);
			}
		}

		return folded;
	}

}
