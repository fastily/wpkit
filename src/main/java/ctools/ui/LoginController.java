package ctools.ui;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FError;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Implements a basic login form
 * 
 * @author Fastily
 *
 */
public class LoginController
{
	/**
	 * The Wiki object which login will be attempted on.
	 */
	private Wiki wiki;

	/**
	 * The root node of this LoginController.
	 */
	private Parent root;

	/**
	 * The method to run on a successful login.
	 */
	private Runnable callback;

	/**
	 * The username text field
	 */
	@FXML
	protected TextField userF;

	/**
	 * The password text field
	 */
	@FXML
	protected PasswordField pxF;
	
	/**
	 * The login Button
	 */
	@FXML
	protected Button loginButton;

	/**
	 * Attempts login, instantiates this object's Wiki object on success. Shows error message on failure.
	 * 
	 * @param e The ActionEvent that triggered this method call1
	 */
	@FXML
	protected void tryLogin(ActionEvent e)
	{
		if (callback == null)
			FError.errAndExit("You need to specify a callback in setOnLoginSuccess before using this method!");

		FXTool.runAsyncTask(() -> {
			try
			{
				Platform.runLater(() -> loginButton.setDisable(true));
				wiki = new Wiki(userF.getText().trim(), pxF.getCharacters().toString(), "en.wikipedia.org");
				Platform.runLater(callback);
				Platform.runLater(() -> ((Stage) ((Node) e.getSource()).getScene().getWindow()).close());
			}
			catch (Throwable x)
			{
				Platform.runLater(() -> {
					FXTool.alertUser("Login Failed!\n\n" + x.getMessage(), Alert.AlertType.ERROR);
					pxF.clear();
					loginButton.setDisable(false);
				});
			}
		});
	}

	/**
	 * Generates a LoginController. PRECONDITION: Login.fxml is in the same directory as this class.
	 * 
	 * @param r The action to perform on a successful user login
	 * @return A new LoginController
	 */
	public static LoginController load(Runnable r)
	{
		FXMLLoader fl = new FXMLLoader(LoginController.class.getResource("Login.fxml"));

		LoginController lc = null;

		try
		{
			Parent root = fl.load();
			lc = fl.getController();
			lc.root = root;
		}
		catch (Throwable e)
		{
			FError.errAndExit(e, "Should never reach this point; Login.fxml is missing?");
		}

		lc.callback = r;
		return lc;
	}

	/**
	 * Gets this object's Wiki
	 * 
	 * @return The Wiki object to get
	 */
	public Wiki getWiki()
	{
		return wiki;
	}

	/**
	 * Gets the root Node of the FXML map
	 * 
	 * @return The root Node.
	 */
	public Parent getRoot()
	{
		return root;
	}
}