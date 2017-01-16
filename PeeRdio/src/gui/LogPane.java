package gui;

import java.awt.Color;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import utils.Constants;
import utils.log.LogEvent;
import utils.log.LogEventListener;
import java.awt.BorderLayout;

/**
 * this class represents the log view, implemented as a layered Pane
 */
public class LogPane extends JLayeredPane implements LogEventListener {
	private static final long serialVersionUID = 1L;
	// This is the message box for all chat and system messages
	private JTextPane tpMessageBoard;
	// scroll bars for message box
	private JScrollPane spScrollBoard;

	
	/**
	 * Constructor
	 */
	public LogPane() {
		initTextFields();
	}
	

	/**
	 * intialize the textfield 'tpMessageBoard' and the scroll area 'spScrollBoard' and add it
	 * to the panel
	 */
	public void initTextFields() {
		setLayout(new BorderLayout(0, 0));
		// text board
		tpMessageBoard = new JTextPane();
		tpMessageBoard.setBounds(0, 0, this.getWidth(), this.getHeight()-40);
		tpMessageBoard.setBorder(null);
		tpMessageBoard.setEditable(false);
		
		// scroll area
		spScrollBoard = new JScrollPane();
		spScrollBoard.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		spScrollBoard.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		spScrollBoard.setViewportView(tpMessageBoard);

		add(spScrollBoard);
	}
	
	/**
	 * this method adds e message to the text area with e specified color
	 * @param message - string
	 * @param color - Color, fontcolor of the message
	 */
	public void appendMessage(String message, Color color) {
		// 
		StyleContext context = StyleContext.getDefaultStyleContext();
	    AttributeSet aset = context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);

		try {
			tpMessageBoard.getDocument().insertString(tpMessageBoard.getDocument().getLength(), message + "\n", aset);
			tpMessageBoard.setCaretPosition(tpMessageBoard.getDocument().getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	    
	}
	
	// text anhaengen ohne Farbangabe - standardfarbe wird benutzt
	public void appendMessage(String message) {
		this.appendMessage(message, Constants.MSG_STANDARD_COLOR);
	}
	
	
	/**
	 * Getter for the Messag
	 * @return tpMessageBoard - JTextPane
	 */
	public JTextPane getTpMessageBoard() {
		return tpMessageBoard;
	}
	
	
	

	@Override
	public void logMessageOccured(LogEvent evt) {
		if(evt == null) return;
		switch(evt.getLogType()) {
		case warning:
			appendMessage(evt.getMessage(), Color.red);
			break;
		case debug: 
			break;
		default:
			appendMessage(evt.getMessage());	
		}	
	}
	
}
