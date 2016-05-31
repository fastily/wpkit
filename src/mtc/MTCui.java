package mtc;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jwiki.core.Wiki;
import ui.FXTool;
import ui.LoginController;

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
	protected static final String version = "0.1";
	
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
		mc.dumpLog();
	}
	
	/**
	 * Creates and shows the main MTC UI
	 * 
	 * @param wiki The Wiki object to use with the UI
	 */
	private void createAndShowMTC(Wiki wiki)
	{
		FXTool.setupAndShowStage(new Stage(), "MTC!", new Scene((mc = MTCController.load(wiki)).getRoot()));
	}
}