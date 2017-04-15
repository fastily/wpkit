package mtc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javafx.application.Platform;
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
	protected ComboBox<String> modeSelect;

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

	// private boolean canCont = false;

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
		mc.modeSelect.getItems().addAll("File", "User", "FileUsage", "Links", "Category", "Template"); // TODO: enum these

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
	 * Transfers files to Commons as per user input.
	 */
	@FXML
	protected void startTransfer()
	{
		String text = textInput.getText().trim(), mode = modeSelect.getSelectionModel().getSelectedItem();
		if (text.isEmpty() || mode == null)
		{
			FXTool.warnUser("Please select a transfer mode and specify a File, Category, Username, or Template to continue.");
			return;
		}

		mtc.ignoreFilter = filterToggle.isSelected();
		pb.setProgress(0);
		console.clear();
		transferButton.setDisable(true); // TODO: add abort logic

		printToConsole("Hold tight, querying server...");
		FXTool.runAsyncTask(() -> runTransfer(mode, text));
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
	 * Performs a transfer with the given transfer mode and text.
	 * 
	 * @param mode The mode of transfer
	 * @param text The input text by the user.
	 */
	private void runTransfer(String mode, String text)
	{
		ArrayList<String> fl;
		switch (mode)
		{
			case "File":
				fl = FL.toSAL(wiki.convertIfNotInNS(text, NS.FILE));
				break;
			case "Category":
				fl = wiki.getCategoryMembers(wiki.convertIfNotInNS(text, NS.CATEGORY), NS.FILE);
				break;
			case "User":
				fl = wiki.getUserUploads(wiki.nss(text));
				break;
			case "Template":
				fl = wiki.whatTranscludesHere(wiki.convertIfNotInNS(text, NS.TEMPLATE), NS.FILE);
				break;
			case "FileUsage":
				fl = wiki.fileUsage(text);
				break;
			case "Links":
				fl = wiki.getLinksOnPage(true, text, NS.FILE);
				break;
			default:
				fl = new ArrayList<>();
				break;
		}

		AtomicInteger success = new AtomicInteger();
		ArrayList<String> fails = new ArrayList<>();

		ArrayList<TransferFile> tol = mtc.filterAndResolve(fl);
		int tolSize = tol.size();

		printToConsole(
				String.format("Analysis complete -> (Total/Filtered/Eligible): (%d/%d/%d)%n", fl.size(), fl.size() - tolSize, tolSize));

		if (tol.isEmpty())
		{
			Platform.runLater(() -> {
				printToConsole(
						"I did not find any file(s) matching your request.  Please verify that your input is correct or disable the smart filter.");
				transferButton.setDisable(false);
			});

			return;
		}

		AtomicInteger cnt = new AtomicInteger();
		tol.stream().forEach(to -> {

			Platform.runLater(() -> {
				pb.setProgress(((double) cnt.getAndIncrement()) / tolSize);
				printToConsole(String.format("Transferring (%d/%d): %s", cnt.get(), tolSize, to.wpFN));
			});

			if (to.doTransfer())
				success.incrementAndGet();
			else
				fails.add(to.wpFN);
		});

		if (!fails.isEmpty())
			Platform.runLater(() -> FXTool
					.warnUser(String.format("Task complete, with %d failures:%n%n%s", fails.stream().collect(Collectors.joining("\n")))));

		Platform.runLater(() -> {
			printToConsole(String.format("Done, completed %d transfer(s)", success.get()));
			pb.setProgress(1);

			transferButton.setDisable(false);
		});
	}

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
}