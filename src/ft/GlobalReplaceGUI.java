package ft;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ft.GlobalReplace.RItem;
import util.FGUI;
import jwiki.core.Settings;
import jwiki.core.Wiki;
import jwiki.util.FSystem;

/**
 * GUI wrapper for GlobalReplace
 * 
 * @author Fastily
 *
 */
public class GlobalReplaceGUI
{
	/**
	 * The wiki object to use
	 */
	private static Wiki wiki;

	/**
	 * Program's name
	 */
	private static final String id = "GlobalReplace 0.4";

	/**
	 * TextFields for old filename, new filename, and reason
	 */
	private static final JTextField old_tf = new JTextField(30), new_tf = new JTextField(30), r_tf = new JTextField(30);

	/**
	 * The start stop button
	 */
	private static final JButton button = new JButton("Start/Stop");

	/**
	 * Flag indicating if we're live (replacement is occurring)
	 */
	private static boolean isLive = false;

	/**
	 * Our progress bar
	 */
	private static final JProgressBar bar = new JProgressBar(0, 100);

	/**
	 * Main driver
	 * 
	 * @param args Prog args (none accepted)
	 * @throws LoginException Bad credentials
	 */
	public static void main(String[] args) throws LoginException
	{
		wiki = FGUI.login();
		Settings.useragent = String.format("%s on %s with %s", wiki.whoami(), id, Settings.useragent);
		SwingUtilities.invokeLater(() -> createAndShowGUI());
	}

	/**
	 * Create and show the GUI
	 */
	private static void createAndShowGUI()
	{
		String cph = "Hint: Use " + (FSystem.isWindows ? "Ctrl+v" : "Command+v") + " to paste text";
		old_tf.setToolTipText(cph);
		new_tf.setToolTipText(cph);
		r_tf.setToolTipText("Hint: Enter an optional edit summary");
		bar.setStringPainted(true);
		bar.setString(String.format("Hello, %s!!", wiki.whoami()));
		button.addActionListener(e -> {
			if (!isLive)
			{
				isLive = true;
				new Thread(() -> startReplace()).start();
			}
			else
			{
				isLive = false;
				button.setEnabled(false);
			}
		});

		JFrame f = FGUI.simpleJFrame(id, JFrame.EXIT_ON_CLOSE, true);
		f.getContentPane().add(
				FGUI.buildForm(id, new JLabel("Old Title: "), old_tf, new JLabel("New Title: "), new_tf,
						new JLabel("Summary: "), r_tf), BorderLayout.CENTER);
		f.getContentPane().add(FGUI.boxLayout(BoxLayout.Y_AXIS, FGUI.simpleJPanel(button), bar), BorderLayout.SOUTH);
		FGUI.setJFrameVisible(f);
	}

	/**
	 * Perform the replacement process
	 */
	private static void startReplace()
	{
		if (old_tf.getText().trim().isEmpty() || new_tf.getText().trim().isEmpty())
		{
			JOptionPane.showMessageDialog(null, "Old File or New File Text Fields cannot be empty!");
			return;
		}

		toggleFields();
		bar.setValue(0);

		ArrayList<RItem> l = GlobalReplace.makeRItem(wiki, old_tf.getText().trim(), new_tf.getText().trim(), r_tf.getText()
				.trim());
		if (l.isEmpty())
			JOptionPane.showMessageDialog(null, "Nothing to process");
		else
		{
			int top = l.size();
			bar.setMaximum(top);
			for (int i = 0; i < top;)
			{
				if (!isLive)
					break;

				RItem item = l.get(i);
				bar.setString(String.format("Processing %s @ %s → (%d/%d)", item.title, item.domain, i + 1, top));

				item.doJob(wiki);

				bar.setValue(++i);
			}

			bar.setString("Done!");
		}

		button.setEnabled(true);
		toggleFields();
	}

	/**
	 * Flips the enabled property of the text fields to disabled and vice versa.
	 */
	private static void toggleFields()
	{
		boolean toggle = !old_tf.isEnabled();

		old_tf.setEnabled(toggle);
		new_tf.setEnabled(toggle);
		r_tf.setEnabled(toggle);
	}

}