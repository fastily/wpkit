package commons;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import commons.GlobalReplace.RItem;
import util.FNet;
import gui.ConsoleBox;
import gui.FGUI;
import gui.ToggleButton;
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
	private static final ToggleButton button = new ToggleButton("Start");

	/**
	 * Logs events for user.
	 */
	private static final ConsoleBox cBox = new ConsoleBox(String.format("Welcome to %s!%n", id));
	
	/**
	 * Our progress bar
	 */
	private static JProgressBar bar;

	/**
	 * Main driver
	 * 
	 * @param args Prog args (none accepted)
	 * @throws Throwable Bad credentials, idk.
	 */
	public static void main(String[] args) throws Throwable
	{
		wiki = FGUI.login();

		Settings.userAgent = String.format("%s on %s with %s", wiki.whoami(), id, Settings.userAgent);
		new Thread(() -> FNet.get("https://tools.wmflabs.org/commonstools/globalreplace/GR.php")).start(); //metrics
		
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

		bar = FGUI.makePB(String.format("Hello, %s!", wiki.whoami()));

		button.addActionListener(e -> {
			if (!button.isLive())
				new Thread(() -> startReplace()).start();
			else
				button.setStatus(null, false, false);
		});

		JFrame f = FGUI.simpleJFrame(id, JFrame.EXIT_ON_CLOSE, true);

		f.getContentPane().add(
				FGUI.topBottomBorderMerge(FGUI.buildForm(id, new JLabel("Old Title: "), old_tf, new JLabel("New Title: "),
						new_tf, new JLabel("Summary: "), r_tf), FGUI.boxLayout(BoxLayout.Y_AXIS, FGUI.simpleJPanel(button), bar)),
				BorderLayout.NORTH);
		f.getContentPane().add(FGUI.borderTitleWrap(cBox.jsp, "Log"), BorderLayout.CENTER);
		FGUI.setJFrameVisible(f);

	}

	/**
	 * Perform the replacement process
	 */
	private static void startReplace()
	{
		if (FGUI.tcIsEmpty(old_tf) || FGUI.tcIsEmpty(new_tf))
		{
			cBox.error("'Old Title' or 'New Title' cannot be empty!");
			return;
		}

		toggleFields();
		cBox.fyi("Starting job!");
		button.setStatus("Stop", true, true);

		ArrayList<RItem> l = GlobalReplace.makeRItem(wiki, wiki.nss(FGUI.getTCText(old_tf)),
				wiki.nss(FGUI.getTCText(new_tf)), FGUI.getTCText(r_tf));
		if (l.isEmpty())
		{
			fyiAndReset("Found nothing to process for " + FGUI.getTCText(old_tf), "");
			return;
		}

		cBox.clear(8000);

		int top = l.size();
		bar.setValue(0);
		bar.setMaximum(top);
		for (int i = 0; i < top;)
		{
			if (!button.isLive())
			{
				fyiAndReset("Stopped!", "Stopped!");
				bar.setValue(top);
				return;
			}

			RItem item = l.get(i);
			bar.setString(String.format("%.2f%%", bar.getPercentComplete() * 100));
			cBox.info(String.format("Processing item [%s @ %s] (%d/%d)", item.title, item.domain, i + 1, top));

			if (!item.doJob(wiki))
				cBox.fyi(String.format("Skipping [%s @ %s]!", item.title, item.domain));

			bar.setValue(++i);
		}

		fyiAndReset("Done!", "Done!");
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

	/**
	 * Logs an FYI event to user's console and sets gui back to near default.
	 * 
	 * @param logtext The text to log
	 * @param pbtext The text to set the progress bar to.
	 */
	private static void fyiAndReset(String logtext, String pbtext)
	{
		toggleFields();
		cBox.fyi(logtext);
		button.setStatus("Start", true, false);
		if (pbtext != null)
			bar.setString(pbtext);
	}

}