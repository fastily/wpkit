package mtc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.FSystem;
import ctools.ui.FXTool;

/**
 * An MTC UI window
 * 
 * @author Fastily
 *
 */
public class MTCController
{
	@FXML
	protected TextArea console;

	/**
	 * The ProgressBar for the UI
	 */
	@FXML
	protected ProgressBar pb;

	/**
	 * The ComboBox for mode selection
	 */
	@FXML
	protected ComboBox<MTC.TransferMode> modeSelect;

	/**
	 * The TextField for user input
	 */
	@FXML
	protected TextField textInput;

	/**
	 * UI component toggling the smart filter
	 */
	@FXML
	protected CheckMenuItem filterToggle;

	/**
	 * UI component toggling the post-transfer delete function
	 */
	@FXML
	protected CheckMenuItem deleteToggle;

	/**
	 * The transfer Button
	 */
	@FXML
	protected Button transferButton;

	/**
	 * The paste Button
	 */
	@FXML
	protected Button pasteButton;

	/**
	 * The user Label and ProgressBar
	 */
	@FXML
	protected Label userLabel;

	/**
	 * The root Node object for the UI
	 */
	private Parent root;

	/**
	 * The Wiki objects to use with MTC.
	 */
	private Wiki wiki;

	/**
	 * The most recently created TransferTask. This may or may not be running.
	 */
	private TransferTask currTask = null;

	/**
	 * The MTC instance for this Controller.
	 */
	private static MTC mtc;

	/**
	 * The OS clipboard
	 */
	private static Clipboard clipboard = Clipboard.getSystemClipboard();

	/**
	 * Date format for prefixing output.
	 */
	private static DateTimeFormatter df = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a");

	/**
	 * The MTCController
	 * 
	 * @param wiki The Wiki object to use
	 * @return The MTC controller
	 */
	public static MTCController load(Wiki wiki)
	{
		FXMLLoader fl = new FXMLLoader(MTCController.class.getResource("MTC.fxml"));

		Parent root = null;
		try
		{
			root = fl.load();
		}
		catch (Throwable e)
		{
			FXTool.warnUser("Should never reach this point; MTC.fxml is missing?");
			FSystem.errAndExit(e, null);
		}

		MTCController mc = fl.getController();

		mc.root = root;
		mc.wiki = wiki;

		// Initialize dynamic data for Nodes
		mc.userLabel.setText("Hello, " + mc.wiki.whoami());
		mc.modeSelect.getItems().addAll(MTC.TransferMode.values());

		// Initalize MTC base
		try
		{
			mtc = new MTC(mc.wiki);
		}
		catch (Throwable e)
		{
			FXTool.warnUser("Are you missing filesystem Read/Write Permissions?");
			FSystem.errAndExit(e, null);
		}
		
		return mc;
	}

	/**
	 * Pastes a String (if available) from the OS clipboard into {@code textInput}.
	 */
	@FXML
	protected void doPaste()
	{
		if (clipboard.hasString())
			textInput.setText(clipboard.getString());
	}

	/**
	 * Transfers files to Commons as per user input.
	 */
	@FXML
	protected void triggerAction()
	{
		if (currTask == null || currTask.isDone())
		{
			String text = textInput.getText().trim();
			MTC.TransferMode mode = modeSelect.getSelectionModel().getSelectedItem();

			if (text.isEmpty() || mode == null)
				FXTool.warnUser("Please select a transfer mode and specify a File, Category, Username, or Template to continue.");
			else
				new Thread(currTask = new TransferTask(mode, text)).start();
		}
		else if (currTask.isRunning())
			currTask.cancel(false);
	}

	/**
	 * Adds a time-stamped message to the {@code console} TextArea.
	 * 
	 * @param msg The new message to add.
	 */
	private void printToConsole(String msg)
	{
		console.appendText(String.format("(%s): %s%n", LocalDateTime.now().format(df), msg));
	}

	/**
	 * Gets this controller's root Node
	 * 
	 * @return The root Node.
	 */
	public Parent getRoot()
	{
		return root;
	}

	/**
	 * Business logic for transferring a set of file(s) to Commons.
	 * 
	 * @author Fastily
	 *
	 */
	private class TransferTask extends Task<Void>
	{
		/**
		 * The mode of transfer to attempt.
		 */
		private MTC.TransferMode mode;

		/**
		 * The text collected from user input.
		 */
		private String text;

		/**
		 * Titles of all files which could not be transferred.
		 */
		private ArrayList<String> fails = new ArrayList<>();

		/**
		 * Constructor, creates a new TransferTask.
		 * 
		 * @param mode The TransferMode to use.
		 * @param text The text input from the user.
		 */
		private TransferTask(MTC.TransferMode mode, String text)
		{
			this.mode = mode;
			this.text = text;

			mtc.ignoreFilter = filterToggle.isSelected();

			messageProperty().addListener((obv, o, n) -> printToConsole(n));
			stateProperty().addListener((obv, o, n) -> {
				switch (n)
				{
					case SCHEDULED:
						console.clear();
						transferButton.setText("Cancel");
						updateProgress(0, 1);
						break;
					case CANCELLED:
					case SUCCEEDED:
					case FAILED:
						transferButton.setText("Start");
						updateProgress(1, 1);
						break;

					default:
						break;
				}
			});

			setOnCancelled(e -> updateMessage("You cancelled this transfer!"));
			setOnFailed(e -> updateMessage("Something's not right.  Are you connected to the internet?"));
			setOnSucceeded(e -> updateMessage(String.format("Task succeeded, with %d failures: %s", fails.size(), fails)));

			pb.progressProperty().bind(progressProperty());
		}

		/**
		 * Performs the actual file transfer(s).
		 */
		public Void call()
		{
			updateMessage("Please wait, querying server...");

			ArrayList<String> fl;
			switch (mode)
			{
				case FILE:
					fl = FL.toSAL(wiki.convertIfNotInNS(text, NS.FILE));
					break;
				case CATEGORY:
					fl = wiki.getCategoryMembers(wiki.convertIfNotInNS(text, NS.CATEGORY), NS.FILE);
					break;
				case USER:
					fl = wiki.getUserUploads(wiki.nss(text));
					break;
				case TEMPLATE:
					fl = wiki.whatTranscludesHere(wiki.convertIfNotInNS(text, NS.TEMPLATE), NS.FILE);
					break;
				case FILEUSAGE:
					fl = wiki.fileUsage(text);
					break;
				case LINKS:
					fl = wiki.getLinksOnPage(true, text, NS.FILE);
					break;
				default:
					fl = new ArrayList<>();
					break;
			}

			ArrayList<TransferFile> tol = mtc.filterAndResolve(fl);
			int tolSize = tol.size();

			// Checkpoint - kill Task now if cancelled
			if (isCancelled())
				return null;

			updateMessage(String.format("[Total/Filtered/Eligible]: [%d/%d/%d]", fl.size(), fl.size() - tolSize, tolSize));

			if (tol.isEmpty())
			{
				updateMessage("Found no file(s) matching your request; verify your input(s) and/or disable the smart filter.");
				updateProgress(0, 1);
			}
			else
				for (int i = 0; i < tol.size() && !isCancelled(); i++)
				{
					TransferFile to = tol.get(i);

					updateProgress(i, tolSize);
					updateMessage(String.format("Transfer [%d/%d]: %s", i, tolSize, to.wpFN));

					if (!to.doTransfer())
						fails.add(to.wpFN);
				}

			return null;
		}
	}
}