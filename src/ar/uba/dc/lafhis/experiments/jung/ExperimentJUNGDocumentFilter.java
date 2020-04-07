package ar.uba.dc.lafhis.experiments.jung;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;


public class ExperimentJUNGDocumentFilter extends DocumentFilter {
    private final StyledDocument styledDocument;
    private final JTextPane pane;

    private final StyleContext styleContext;
    private final SimpleAttributeSet keywordAttributeSet;
    private final SimpleAttributeSet defaultAttributeSet;
    private final SimpleAttributeSet commentAttributeSet;
    private final SimpleAttributeSet operatorsAttributeSet;

    private static String[] KEYWORDS = {"fluent","set","range","const","when","initially","ltl","env","sys","rho","theta","in","order","equals"};
    
    // Use a regular expression to find the words you are looking for
    private Pattern keywordPattern;
    private Pattern commentPattern;
    private Pattern operatorsPattern;

    public ExperimentJUNGDocumentFilter(JTextPane pane) {
    	this.pane		= pane;
    	styledDocument	= pane.getStyledDocument();
    	keywordPattern 	= buildKeywordPattern();
    	commentPattern	= Pattern.compile("/\\*.*\\n.*\\*\\/|/\\*.*\\*\\/|//.*");
    	operatorsPattern= Pattern.compile("\\{|\\}|<|>|\\.|\\(|\\)|-|=|,|\\|\\||\\|f\\||\\|gr1\\|");    	
    	
        styleContext = StyleContext.getDefaultStyleContext();
        keywordAttributeSet = new SimpleAttributeSet();
        commentAttributeSet = new SimpleAttributeSet();
        defaultAttributeSet = new SimpleAttributeSet();
        operatorsAttributeSet	= new SimpleAttributeSet();
        setDefaultAttributes(keywordAttributeSet);
        setDefaultAttributes(commentAttributeSet);
        setDefaultAttributes(defaultAttributeSet);
        setDefaultAttributes(operatorsAttributeSet);
        
        StyleConstants.setForeground(defaultAttributeSet, new Color(238,232,213));
        StyleConstants.setForeground(keywordAttributeSet, new Color(211,54,130));//magenta
        StyleConstants.setForeground(commentAttributeSet, Color.LIGHT_GRAY);
        StyleConstants.setForeground(operatorsAttributeSet, new Color(203,75,22));//orange
	}
    
    private void setDefaultAttributes(SimpleAttributeSet attrSet) {
    	StyleConstants.setBackground(attrSet, Color.BLACK);
    	StyleConstants.setFontFamily(attrSet, "Courier New");
    	StyleConstants.setFontSize(attrSet, 12);
    }
    
    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attributeSet) throws BadLocationException {
        super.insertString(fb, offset, text, attributeSet);

        handleTextChanged();
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);

        handleTextChanged();
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attributeSet) throws BadLocationException {
        super.replace(fb, offset, length, text, attributeSet);

        handleTextChanged();
    }

    /**
     * Runs your updates later, not during the event notification.
     */
    private void handleTextChanged()
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateTextStyles();
            }
        });
    }


    private Pattern buildKeywordPattern()
    {
        StringBuilder sb = new StringBuilder();
        boolean notFirst = false;
        
        for (String token : KEYWORDS) {
        	if(notFirst) {sb.append("|");}
        	else {notFirst = true;}
        	sb.append("\\b");
            sb.append(token);
            sb.append("\\b");
        }

        System.out.println(sb.toString());
        Pattern p = Pattern.compile(sb.toString());

        return p;
    }


    private void updateTextStyles()
    {
        styledDocument.setCharacterAttributes(0, pane.getText().length(), defaultAttributeSet, true);

        Matcher matcher = keywordPattern.matcher(pane.getText());
        while (matcher.find()) {
            styledDocument.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), keywordAttributeSet, false);
        }
        matcher = commentPattern.matcher(pane.getText());
        while (matcher.find()) {
            styledDocument.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), commentAttributeSet, false);
        }
        matcher = operatorsPattern.matcher(pane.getText());
        while (matcher.find()) {
            styledDocument.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), operatorsAttributeSet, false);
        }
    }
}