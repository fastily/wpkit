package ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import jwiki.core.Wiki;
import jwiki.util.FError;

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
	 * The method to run on a successful login.
	 */
	private Runnable callback;

	/**
	 * The root node of this LoginController.
	 */
	private Parent root;

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
				wiki = new Wiki(userF.getText().trim(), pxF.getCharacters().toString(), "commons.wikimedia.org");
				Platform.runLater(callback);
				Platform.runLater(() -> ((Stage) ((Node) e.getSource()).getScene().getWindow()).close());
			}
			catch (Throwable x)
			{
				Platform.runLater(() -> {
					new Alert(Alert.AlertType.ERROR, "Login Failed!", ButtonType.OK).showAndWait();
					pxF.clear();
				});
			}
		});
	}

	/**
	 * Runs a Runnable after a successful login
	 * 
	 * @param r The Runnable to run
	 */
	public void setOnLoginSuccess(Runnable r)
	{
		callback = r;
	}

	/**
	 * Gets the Wiki object associated with this controller.
	 * 
	 * @return The Wiki object associated with this controller.
	 */
	public Wiki getWiki()
	{
		return wiki;
	}

	/**
	 * Gets the root node of this LoginController.
	 * 
	 * @return The root node of this LoginController.
	 */
	public Parent getRoot()
	{
		return root;
	}

	/**
	 * Generates a LoginController. PRECONDITION: Login.fxml is in the same directory as this class.
	 * 
	 * @param c The class calling this method
	 * @return A new LoginController
	 */
	public static LoginController load(Class<?> c)
	{
		FXMLLoader fl = new FXMLLoader(c.getResource("Login.fxml"));

		LoginController lc = null;

		try
		{
			Parent root = fl.load();
			lc = fl.getController();
			lc.root = root;
		}
		catch (Throwable e)
		{
			FError.errAndExit(e, "Login.fxml is missing?");
		}

		return lc;
	}
}