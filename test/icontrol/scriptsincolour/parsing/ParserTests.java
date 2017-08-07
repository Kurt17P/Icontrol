package icontrol.scriptsincolour.parsing;

import static icontrol.scriptsincolour.parsing.Type.BackSlash;
import static icontrol.scriptsincolour.parsing.Type.Dash;
import static icontrol.scriptsincolour.parsing.Type.DoubleQuote;
import static icontrol.scriptsincolour.parsing.Type.FloatNumber;
import static icontrol.scriptsincolour.parsing.Type.IPAddress;
import static icontrol.scriptsincolour.parsing.Type.IntNumber;
import static icontrol.scriptsincolour.parsing.Type.Period;
import static icontrol.scriptsincolour.parsing.Type.Plus;
import static icontrol.scriptsincolour.parsing.Type.Quoted;
import static icontrol.scriptsincolour.parsing.Type.FwdSlash;
import static icontrol.scriptsincolour.parsing.Type.SingleQuote;
import static icontrol.scriptsincolour.parsing.Type.Whitespace;
import static icontrol.scriptsincolour.parsing.Type.Word;
import static icontrol.scriptsincolour.parsing.Type.Colon;
import static icontrol.scriptsincolour.parsing.Type.SemiColon;
import static icontrol.scriptsincolour.parsing.Type.OpenRound;
import static icontrol.scriptsincolour.parsing.Type.CloseRound;
import static icontrol.scriptsincolour.parsing.Type.Equals;
import static icontrol.scriptsincolour.parsing.Type.Underscore;
import static icontrol.scriptsincolour.parsing.Type.Keyword;

import icontrol.scriptsincolour.ui.Keywords;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Various tests to exercise the accuracy of the parser (tokenizer & tokenFolder combo)
 * 
 * @author Brian Remedios
 */
public class ParserTests {

	// test format:  test string -> (N primitive tokens) -> null -> (N folded tokens)    where a possible null acts as separator between the two types
	
	public static final Object[][] Tests = new Object[][] {
		{ "one", Word },
		{ "one two", Word, Whitespace, Word },
		{ "!@#$%^&*()_ two", Type.Exclamation, Type.AtSign, Type.Hash, Type.Dollar, Type.Percent, Type.Caret, Type.Ampersand, Type.Star, Type.OpenRound, Type.CloseRound, Type.Underscore },
		{ "3 4 5", IntNumber, Whitespace, IntNumber, Whitespace, IntNumber },
		{ "3.14159", IntNumber, Period, IntNumber, null, FloatNumber },
		{ "2e+12", IntNumber, Word, Plus, IntNumber, null, IntNumber },
		{ "1e6", IntNumber, Word, IntNumber, null, IntNumber },
		{ "1.4e6", IntNumber, Period, IntNumber, Word, IntNumber, null, FloatNumber },
		{ "1.2e+12", IntNumber, Period, IntNumber, Word, Plus, IntNumber, null, FloatNumber },	// actually an invalid conversion, still an integer (TODO check this)
		{ "1.2e-12", IntNumber, Period, IntNumber, Word, Dash, IntNumber, null, FloatNumber },
		{ "1.0E-52", IntNumber, Period, IntNumber, Word, Dash, IntNumber, null, FloatNumber },
		{ "1.0E-52  -4.5", IntNumber, Period, IntNumber, Word, Dash, IntNumber, Whitespace, Dash, IntNumber, Period, IntNumber, null, FloatNumber, Whitespace, FloatNumber },
		{ "   ", Whitespace },
		{ "  \t\t ", Whitespace },
		{ " \"big cat\" ", Whitespace, DoubleQuote, Word, Whitespace, Word, DoubleQuote, Whitespace, null, Whitespace, DoubleQuote, Quoted, DoubleQuote, Whitespace },
		{ " 'big cat' ", Whitespace, SingleQuote, Word, Whitespace, Word, SingleQuote, Whitespace, null, Whitespace, SingleQuote, Quoted, SingleQuote, Whitespace },
		{ "'a \\'quote\\' item'", SingleQuote, Word, Whitespace, BackSlash, SingleQuote, Word, BackSlash, SingleQuote, Whitespace, Word, SingleQuote, null, SingleQuote, Quoted, SingleQuote },
		{ "192.168.1", 		IntNumber, Period, IntNumber, Period, IntNumber, null, IntNumber, Period, IntNumber, Period, IntNumber},
		{ "192.168.1.", 	IntNumber, Period, IntNumber, Period, IntNumber, Period, null, IntNumber, Period, IntNumber, Period, IntNumber, Period},
		{ "192.168.1.256", 	IntNumber, Period, IntNumber, Period, IntNumber, Period, IntNumber, null, IntNumber, Period, IntNumber, Period, IntNumber, Period, IntNumber},	// 256 = out-of-range
		{ "192.168.1.255", 	IntNumber, Period, IntNumber, Period, IntNumber, Period, IntNumber, null, IPAddress},
		{ "02.068.1.205", 	IntNumber, Period, IntNumber, Period, IntNumber, Period, IntNumber, null, IPAddress},
		{ "10.0 / 1e6", 	IntNumber, Period, IntNumber, Whitespace, FwdSlash, Whitespace, IntNumber, Word, IntNumber, null, FloatNumber, Whitespace, FwdSlash, Whitespace, IntNumber },
		{ "fred123", Word, IntNumber, null, Word },
		{ "fred_123", Word, Underscore, IntNumber, null, Word },
		{ "fred_123_", Word, Underscore, IntNumber, Underscore, null, Word },
		{ "MAKE osci; Yokogawa DL9000; TMCTL: USBTMC(DL9000) = 27E826755", 
			Word, Whitespace, Word, SemiColon, Whitespace, Word, Whitespace, Word, IntNumber, SemiColon, Whitespace, Word, Colon, Whitespace, Word, OpenRound, Word, IntNumber, CloseRound, Whitespace, Equals, Whitespace, IntNumber, Word, IntNumber, null,
			Keyword, Whitespace, Word, SemiColon, Whitespace, Word, Whitespace, Word, SemiColon, Whitespace, Keyword, Colon, Whitespace, Word, OpenRound, Word, CloseRound, Whitespace, Equals, Whitespace, IntNumber
			},
		{ "MAKE baloney", Word, Whitespace, Word, null, Keyword, Whitespace, Word },
		};
	
	@Test
	public void testTokenize() {
	
		Tokenizer tok = new Tokenizer();
		
		Object expected;
		Type actual;
		
		for (Object[] test : Tests) {
			
			List<Token> tokens = tok.tokenize((String)test[0]);
			
			for (int i=0; i<test.length-1; i++) {
				expected = test[i+1];
				if (expected == null) break;	// end of non-folded token sequence 
				actual = tokens.get(i).type;
				Assert.assertEquals(expected, actual);
			}
		}
	}
	
	/**
	 * Scan the items looking for the null element, the next item
	 * denotes the start of the folded tokens we want to compare with.
	 * Returns -1 if none is found.
	 *   
	 * @param items
	 * @return int
	 */
	private static int foldedTokenStart(Object[] items) {
		
		for (int i=1; i<items.length-1; i++) {
			if (items[i] == null) return i+1;
		}
		return -1;
	}
	
	@Test
	public void testFolder() {
	
		Tokenizer tok = new Tokenizer();		
		TokenFolder folder = new TokenFolder(Keywords.IControl);
		
		Object expected;
		Type actual;
		
		for (Object[] test : Tests) {
			
			int foldedIdx = foldedTokenStart(test);
			if (foldedIdx < 0) continue;	// no folded activity here
			
			List<Token> tokens = tok.tokenize((String)test[0]);
			List<Token> folded = folder.fold(tokens);
						
			for (int i=foldedIdx; i<test.length; i++) {
				expected = test[i];
				actual = folded.get(i-foldedIdx).type;
				Assert.assertEquals(expected, actual);
			}
		}
	}
}
