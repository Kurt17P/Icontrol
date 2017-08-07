package icontrol.scriptsincolour.ui;

import static icontrol.scriptsincolour.parsing.Type.Apostrophe;
import static icontrol.scriptsincolour.parsing.Type.BackSlash;
import static icontrol.scriptsincolour.parsing.Type.CloseChevron;
import static icontrol.scriptsincolour.parsing.Type.CloseCurly;
import static icontrol.scriptsincolour.parsing.Type.CloseRound;
import static icontrol.scriptsincolour.parsing.Type.CloseSquare;
import static icontrol.scriptsincolour.parsing.Type.Colon;
import static icontrol.scriptsincolour.parsing.Type.Comma;
import static icontrol.scriptsincolour.parsing.Type.Comment;
import static icontrol.scriptsincolour.parsing.Type.Dash;
import static icontrol.scriptsincolour.parsing.Type.Dollar;
import static icontrol.scriptsincolour.parsing.Type.DoubleQuote;
import static icontrol.scriptsincolour.parsing.Type.Equals;
import static icontrol.scriptsincolour.parsing.Type.FloatNumber;
import static icontrol.scriptsincolour.parsing.Type.FwdSlash;
import static icontrol.scriptsincolour.parsing.Type.Hash;
import static icontrol.scriptsincolour.parsing.Type.IPAddress;
import static icontrol.scriptsincolour.parsing.Type.IntNumber;
import static icontrol.scriptsincolour.parsing.Type.Keyword;
import static icontrol.scriptsincolour.parsing.Type.OpenChevron;
import static icontrol.scriptsincolour.parsing.Type.OpenCurly;
import static icontrol.scriptsincolour.parsing.Type.OpenRound;
import static icontrol.scriptsincolour.parsing.Type.OpenSquare;
import static icontrol.scriptsincolour.parsing.Type.Percent;
import static icontrol.scriptsincolour.parsing.Type.Period;
import static icontrol.scriptsincolour.parsing.Type.Pipe;
import static icontrol.scriptsincolour.parsing.Type.Plus;
import static icontrol.scriptsincolour.parsing.Type.Question;
import static icontrol.scriptsincolour.parsing.Type.Quoted;
import static icontrol.scriptsincolour.parsing.Type.SemiColon;
import static icontrol.scriptsincolour.parsing.Type.SingleQuote;
import static icontrol.scriptsincolour.parsing.Type.Star;
import static icontrol.scriptsincolour.parsing.Type.Whitespace;
import static icontrol.scriptsincolour.ui.Colours.brown;
import static java.awt.Color.black;
import static java.awt.Color.blue;
import static java.awt.Color.darkGray;
import static java.awt.Color.gray;
import static java.awt.Color.red;
import icontrol.scriptsincolour.parsing.Token;
import icontrol.scriptsincolour.parsing.TokenFolder;
import icontrol.scriptsincolour.parsing.Tokenizer;
import icontrol.scriptsincolour.parsing.Type;
import icontrol.scriptsincolour.parsing.Util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * A renderer that recognizes elements of iControl and Python script 
 * command lines and renders them in colour after parsing them.
 * 
 * It can be tardy when loading lengthy scripts but this can be
 * optimized elsewhere - br
 * 
 * The constructor is private, reference the static PythonInstance instead.
 * 
 * Improvements: 
 *    Replace the raw string input with something that can cache the 
 *    parse tokens, retain error info, and possible breakpoints.
 *    Expand the style builder to accept font params.
 * 
 * @author Brian Remedios
 */
@SuppressWarnings("serial")
public class ScriptLineRenderer extends JLabel implements ListCellRenderer<String> {

	private final Tokenizer tokenizer = new Tokenizer();
	
	private final TokenFolder pythonFolder = new TokenFolder(Keywords.Python);
	private final TokenFolder iControlFolder = new TokenFolder(Keywords.IControl);
	
	private static final String PythonFlag = "|";
	
	public static final ScriptLineRenderer Instance = new ScriptLineRenderer();
	
	private static final Style defaultStyle = styleFor("default", Color.black, null);

	private static final Map<Type, Style> StylesByType = new HashMap<Type, Style>();
	
	private static final Set<Type> Punctuation = new HashSet<Type>(
			Arrays.asList(
					Apostrophe, Period, Colon, SemiColon, OpenRound, CloseRound, Comma,
					SingleQuote, DoubleQuote, FwdSlash, BackSlash, 
					Equals, Pipe, Hash, Star, Plus, Dash, Dollar,
					Percent, OpenCurly, OpenChevron, CloseChevron, CloseCurly,
					OpenSquare, CloseSquare, Question
					)
			);

	private static final Insets Margins = new Insets(0,4,0,0);

	static {
		StylesByType.put(Comment, 		styleFor("comment", gray, null));
		StylesByType.put(Quoted,		styleFor("quoted",	blue, null));
		StylesByType.put(IntNumber, 	styleFor("integer", red, null));
		StylesByType.put(FloatNumber, 	styleFor("float", 	red, null));
		StylesByType.put(IPAddress, 	styleFor("ipAddr",	darkGray, null));
		StylesByType.put(Whitespace, 	styleFor("blank", 	black, null));
		StylesByType.put(Keyword, 		styleFor("keyword", brown, null));
	}
	
	private static Style styleFor(Type type) {
		return StylesByType.get(type);
	}

	private static boolean isPunctuation(Token t) {
		return Punctuation.contains(t.type);
	}

	private static void insert(StyledDocument doc, String txt, Style style) {
		try {
			doc.insertString(doc.getLength(), txt, style);
		} catch (BadLocationException ex) {
			// ignore, won't happen
		}
	}
	
	/**
	 * A <code>Style</code> constructor that applies two possible colours.
	 * TODO accept font info as well.
	 *  
	 * @param name
	 * @param foreground
	 * @param background
	 * 
	 * @return Style
	 */
	private static Style styleFor(String name, Color foreground, Color background) {
		
		Style st = StyleContext.getDefaultStyleContext().addStyle(name, null);
		if (foreground != null) StyleConstants.setForeground(st, foreground);
		if (background != null) StyleConstants.setBackground(st, background);
		
		return st;
	}

	/**
	 * Load the required styles onto the specified document.
	 *
	 * @param doc
	 */
	private static void setupStyles(StyledDocument doc) {
		
		Style st;
		for (Entry<Type, Style> entry : StylesByType.entrySet()) {
			st = entry.getValue();
			doc.addStyle(st.getName(), st);
		}		
	}

	private ScriptLineRenderer() { 
		
	}

	/**
	 * Convert the command line into a set of high-level tokens we can work with.
	 * 
	 * @param line
	 *
	 * @return Queue<Token>
	 */
	private Queue<Token> queuedTokensFor(String line) {
		List<Token> tokens = tokenizer.tokenize(line);
		List<Token> folded = line.startsWith(PythonFlag) ? 
				pythonFolder.fold(tokens) :
				iControlFolder.fold(tokens);

		return new LinkedList<Token>(folded);
	}
	
	/**
	 * 
	 * @param doc
	 * @param line
	 * @param isSelected
	 * @param cellHasFocus
	 */
	private void renderOnto(StyledDocument doc, String line, boolean isSelected, boolean cellHasFocus) {

		if (Util.isWhitespace(line)) return;			

		Queue<Token> tokens = queuedTokensFor(line);
		if (tokens.isEmpty()) return;	// unlikely
		
		setupStyles(doc);		

		Style style;
		Token current;

		while (!tokens.isEmpty()) {
			
			current = tokens.remove();
			style = styleFor(current.type);
			
			if (style != null) {
				insert(doc, current.text, style);
				continue;        		
			}

			if (isPunctuation(current)) {
				insert(doc, current.text, defaultStyle);
				continue;
			}
			
			insert(doc, current.text, defaultStyle);
		}
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends String> list, String line, int index, boolean isSelected, boolean cellHasFocus) {

		JTextPane textPane = new JTextPane();

		textPane.setBackground( isSelected ? list.getSelectionBackground() : list.getBackground());
		// TODO render the dashed focus rectangle 
		
		textPane.setMargin(Margins);

		renderOnto(textPane.getStyledDocument(), line, isSelected, cellHasFocus);

		return textPane;
	}

}
