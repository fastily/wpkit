package mtc;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import fastily.jwiki.core.Wiki;
import jwikix.ui.FXTool;
import jwikix.ui.LoginController;
import util.Toolbox;

/**
 * A GUI wrapper for MTC
 * 
 * @author Fastily
 */
public class MTCui extends Application
{
	/**
	 * Version number
	 */
	protected static final String version = "0.1.1";
	
	/**
	 * The title of the page with the minimum version number
	 */
	private static final String serverVersionPage = Config.fullname + "/Version";
	
	/**
	 * The LoginController for this Application
	 */
	private static LoginController lc;

	/**
	 * The MTCController for this Application
	 */
	private static MTCController mc;
	
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
	public void start(Stage primaryStage) throws Exception
	{
		FXTool.setupAndShowStage(primaryStage, "MTC!",
				new Scene((lc = LoginController.load(() -> createAndShowMTC(lc.getWiki()))).getRoot()));
	}

	/**
	 * Dumps a log of transferred items to the user's userspace.
	 */
	public void stop()
	{
		if(mc != null) // Exit cleanly, in case user exits at login
			mc.dumpLog();
	}
	
	/**
	 * Creates and shows the main MTC UI.  Also checks the minimum allowed version.
	 * 
	 * @param wiki The Wiki object to use with the UI
	 */
	private void createAndShowMTC(Wiki wiki)
	{
		String minVersion =  wiki.getPageText(serverVersionPage).trim();
		if(!Toolbox.versionCheck(version, minVersion))
		{
			FXTool.warnUser(String.format("Your version of MTC (%s) is outdated.  The new version is (%s).  Please download the newest version.", version, minVersion)); 
			getHostServices().showDocument("https://en.wikipedia.org/wiki/" + Config.fullname);

			Platform.exit();
		}
		
		FXTool.setupAndShowStage(new Stage(), "MTC!", new Scene((mc = MTCController.load(wiki)).getRoot()));
	}
}