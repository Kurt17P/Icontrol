package icontrol.scriptsincolour.parsing;

import static icontrol.scriptsincolour.parsing.Type.IntNumber;
import static icontrol.scriptsincolour.parsing.Type.Whitespace;
import static icontrol.scriptsincolour.parsing.Type.Word;
import static icontrol.scriptsincolour.parsing.Util.isInteger;
import static icontrol.scriptsincolour.parsing.Util.isWhitespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * A simple tokenizer that emits a sequence of simple tokens for the string passed 
 * in via the tokenize() method.
 * 
 * Note that it doesn't recognize token sequences as more complex that words or
 * integer values. Conversion into higher-level tokens is done by the <code>TokenFolder</code>
 * which can be application or target-specific.
 * 
 * @author Brian Remedios
 */
public class Tokenizer {

	private boolean buildingWord;
	private StringBuilder buff = new StringBuilder();
	private List<Token> tokens = new ArrayList<>();
	
	public Tokenizer() { }

	private void endBuffer() {
		
		if (buff.length() > 0) {
			String contents = buff.toString();
			buff.setLength(0);
			if (isWhitespace(contents)) {
				tokens.add( new Token(Whitespace, contents) );
				return;
				}
			if (isInteger(contents)) {
				tokens.add( new Token(IntNumber, contents) );
				return;
				};
			tokens.add( new Token(Word, contents) );
		}
	}
	
	private char lastBuffChar() {
		int end = buff.length()-1;
		return buff.substring(end, end+1).charAt(0);
	}
	
	/**
	 * Identifiers can include numbers but they generally can't 
	 * start with them so this detects a possible split point.
	 *
	 * @param ch
	 * @return boolean
	 */
	private boolean isDigit_LetterChange(char ch) {
		char lastCh = lastBuffChar();
		if (Character.isDigit(lastCh) && Character.isDigit(ch)) return false;
		if (Character.isAlphabetic(lastCh) && Character.isAlphabetic(ch)) return false;
		return true;
	}
	
	private void addChar(Type charType) {
		if (charType.letter == null || charType.letter.length() > 1) {
			// TODO problem here - a non char type
		}
		endBuffer(); tokens.add( new Token(charType, charType.letter) );
	}
	
	private void reset() {
		buff.setLength(0);
		tokens.clear();
	}
	
	private boolean buffHasData() { return buff.length() > 0; }
	
	
	private boolean closeBuffFor(char ch) {
		return !buildingWord || 
				(buffHasData() && isDigit_LetterChange(ch));
	}
	
	public List<Token> tokenize(String text) {
		
		if (text == null || text.length() == 0) return Collections.<Token>emptyList();
		
		reset();
		
		int pos = 0;
		int end = text.length();
		char ch;

		while (pos < end) {
			ch = text.charAt(pos++);
			
			Type typ = Type.forChar(ch);
			if (typ != null) { 
				addChar(typ); 
				continue; 
				}

			if (Character.isLetterOrDigit(ch)) {
				if (closeBuffFor(ch)) endBuffer();
				buildingWord = true;
				buff.append(ch);
				continue;
				}
			if (Character.isWhitespace(ch)) {
				if (buildingWord) endBuffer();
				buff.append(ch);
				buildingWord = false;
				continue;
				}
			}
		
		endBuffer();	// handle any remaining ones

		return tokens;
	}
	
	/**
	 * Accept a test string, parse it, then emit its primitive tokens in order as well
	 * and then spit out the same sequence after processing by the TokenFolder.
	 *
	 * @param args 
	 */
	public static void main(String[] args) {
		
		Tokenizer t = new Tokenizer();
		List<Token> tokens = t.tokenize(args[0]);
		
		for (Token tok : tokens) {
			System.out.println(tok);
		}
		
		System.out.println();
		
		TokenFolder folder = new TokenFolder();
		List<Token> folded = folder.fold(tokens);
		
		for (Token tok : folded) {
			System.out.println(tok);
		}
	}
}
