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
	 * The LoginController for this Application
	 */
	private LoginController lc;
	
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
		lc = LoginController.load(() -> createAndShowMTC(lc.getWiki()));

		primaryStage.setTitle("MTC!");
		primaryStage.setScene(new Scene(lc.getRoot()));
		primaryStage.show();
	}
	
	/**
	 * Creates and shows the main MTC UI
	 * @param wiki The Wiki object to use with the UI 
	 */
	private void createAndShowMTC(Wiki wiki)
	{
		FXTool.setupAndShowStage(new Stage(), "MTC!", new Scene(MTCController.load(wiki).getRoot()));
	}
}