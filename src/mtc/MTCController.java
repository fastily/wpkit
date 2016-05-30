package mtc;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import jwiki.core.Wiki;
import jwiki.util.FError;

/**
 * An MTC UI window
 * @author Fastily
 *
 */
public class MTCController
{
	/**
	 * The ProgressBar for the UI
	 */
	@FXML
	protected ProgressBar pb;

	/**
	 * The ComboBox for mode selection
	 */
	@FXML
	protected ComboBox<String> cb;

	/**
	 * The TextField for user input
	 */
	@FXML
	protected TextField tf;

	/**
	 * The transfer Button
	 */
	@FXML
	protected Button transferButton;
	
	/**
	 * The user Label and ProgressBar
	 */
	@FXML
	protected Label userLabel, pbLabel;
	
	/**
	 * The root Node object for the UI
	 */
	private Parent root;
	
	/**
	 * The Wiki objects to use with MTC.
	 */
	private Wiki enwp, com;
	
	/**
	 * The MTCController
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
			e.printStackTrace();
			FError.errAndExit(e, "Should never reach this point; MTC.fxml is missing?");
		}

		MTCController mc = fl.getController();
		
		mc.root = root;
		mc.enwp = wiki.getWiki("en.wikipedia.org");
		mc.com = wiki.getWiki("commons.wikimedia.org");
		
		mc.userLabel.setText("Hi, " + mc.enwp.whoami());
		
		return mc;
	}
	
	/**
	 * Applies dynamic data to UI
	 */
	public void initialize()
	{
		cb.getItems().addAll("File", "User", "Category", "Template");
	}

	/**
	 * Gets this controller's root Node
	 * @return The root Node.
	 */
	public Parent getRoot()
	{
		return root;
	}
}