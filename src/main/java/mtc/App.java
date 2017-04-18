package mtc;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ctools.util.Toolbox;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FSystem;
import ctools.ui.FXTool;
import ctools.ui.LoginController;

/**
 * A GUI wrapper for MTC
 * 
 * @author Fastily
 */
public class App extends Application
{
	/**
	 * Version number
	 */
	protected static final String version = "1.0.0";
	
	/**
	 * The title of the page with the minimum version number
	 */
	private static final String serverVersionPage = MStrings.fullname + "/Version";
	
	/**
	 * The Wiki object to use.
	 */
	private Wiki wiki = new Wiki("en.wikipedia.org");
		
	/**
	 * Main Driver
	 * 
	 * @param args Program args, not used.
	 */
	public static void main(String[] args)
	{
		launch(args);
	}

	/**
	 * Called when MTC first starts.
	 */
	public void start(Stage stage) throws Exception
	{
		// Check Version
		String minVersion =  wiki.getPageText(serverVersionPage).trim();
		if(!Toolbox.versionCheck(version, minVersion))
		{
			FXTool.warnUser(String.format("Your version of %s (%s) is outdated.  The current version is (%s), please download the newest version.", MStrings.name, version, minVersion)); 
			FXTool.launchBrowser(this, MStrings.enwpURLBase + MStrings.fullname);

			Platform.exit();
		}
		
		// Start Login Window
		FXMLLoader lcLoader = FXTool.makeNewLoader(LoginController.fxmlLoc, LoginController.class);
		stage.setScene(new Scene(lcLoader.load()));
		
		LoginController lc = lcLoader.getController();
		lc.wiki = wiki;
		lc.callback = this::createAndShowMTC;
		
		stage.setTitle(MStrings.name);
		stage.show();
	}
		
	/**
	 * Creates and shows the main MTC UI.  Also checks the minimum allowed version.
	 * 
	 * @param wiki The Wiki object to use with the UI
	 */
	private void createAndShowMTC()
	{
		FXMLLoader lcLoader = FXTool.makeNewLoader(MTCController.fxmlLoc, MTCController.class);
		
      Stage stage = new Stage();	
      
      try
      {
      	stage.setScene(new Scene(lcLoader.load()));
      }
      catch(Throwable e)
      {
      	FSystem.errAndExit(e, "Should never reach here, is your FXML malformed or missing?");
      }
      
      MTCController mtcC = lcLoader.getController();
      mtcC.initData(wiki);
      
      stage.setTitle(MStrings.name);
      stage.show();
	}
}