package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * A user console for a client.  Allows for colored output.
 * 
 * @author Fastily
 *
 */
public class ConsoleBox
{
	/**
	 * The JScrollPane wrapping this console box. Add this to your panels and frames
	 */
	public final JScrollPane jsp;

	/**
	 * The JTextPane backing the class. Allows stylized text display.
	 */
	private JTextPane jtp = new JTextPane();

	/**
	 * Scroll bar from jsp. This allows us to auto-scroll down.
	 */
	private JScrollBar sb;

	/**
	 * The styled document from jtp
	 */
	private StyledDocument doc = jtp.getStyledDocument();

	/**
	 * A custom style for ConsoleBox.
	 */
	private Style style = jtp.addStyle("leStyle", null);

	/**
	 * The date formatter prefixing output.
	 */
	private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a");

	/**
	 * Constructor
	 * 
	 * @param text The String to initially be displayed in the console box
	 */
	public ConsoleBox(String text)
	{
		jtp.setText(text + "\n");
		jtp.setEditable(false);
		jtp.setPreferredSize(new Dimension(100, 200));

		jsp = new JScrollPane(jtp);
		sb = jsp.getVerticalScrollBar();
	}

	/**
	 * Clears the contents of the ConsoleBox
	 */
	public void clear()
	{
		jtp.setText("");
	}

	/**
	 * Clears the box if there are more than <code>max</code> chars.
	 * 
	 * @param max The minimum number of chars that must be in the console box before clearing
	 */
	public void clear(int max)
	{
		if (doc.getLength() > max)
			clear();
	}

	/**
	 * Logs an info event to the console
	 * 
	 * @param text The text to log
	 */
	public void info(String text)
	{
		addText("INFO: " + text, Color.GREEN);
	}

	/**
	 * Logs a warn event to the console
	 * 
	 * @param text The text to log
	 */
	public void warn(String text)
	{
		addText("WARN: " + text, Color.ORANGE);
	}

	/**
	 * Logs an error event to the console
	 * 
	 * @param text The text to log
	 */
	public void error(String text)
	{
		addText("ERROR: " + text, Color.RED);
	}

	/**
	 * Logs an fyi event to the console
	 * 
	 * @param text The text to log
	 */
	public void fyi(String text)
	{
		addText("FYI: " + text, Color.BLUE);
	}

	/**
	 * Add text to the console box
	 * 
	 * @param text The text to add
	 * @param c The color to add it in
	 */
	private void addText(String text, Color c)
	{
		try
		{
			StyleConstants.setForeground(style, Color.BLACK);
			doc.insertString(doc.getLength(), LocalDateTime.now().format(df) + "\n", style);

			StyleConstants.setForeground(style, c);
			doc.insertString(doc.getLength(), text + "\n", style);

			sb.setValue(sb.getMaximum());
		}
		catch (Throwable e)
		{
			// nobody cares
		}
	}

}