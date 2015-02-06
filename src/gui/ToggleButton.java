package gui;

import javax.swing.JButton;

/**
 * Simple thread safe button which can be enabled and disabled. Useful as the trigger point for starting and stopping
 * jobs.
 * 
 * @author Fastily
 *
 */
@SuppressWarnings("serial")
public class ToggleButton extends JButton
{
	/**
	 * The status of this button
	 */
	private boolean live = false;

	/**
	 * Constructor
	 * 
	 * @param text The button's initial text
	 */
	public ToggleButton(String text)
	{
		super(text);
	}

	/**
	 * Sets this button's status.
	 * 
	 * @param text Sets the button's text. Leave null to keep the same
	 * @param enable Set to true if the button should be clickable
	 * @param live Sets the status to indicate whether a job is running.
	 */
	public synchronized void setStatus(String text, boolean enable, boolean live)
	{
		setEnabled(enable);
		this.live = live;
		if (text != null)
			setText(text);
	}

	/**
	 * Gets the flag indicating whether we're live or not
	 * 
	 * @return True if this button is active.
	 */
	public synchronized boolean isLive()
	{
		return live;
	}
}