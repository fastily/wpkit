package ui;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Miscellaneous static JavaFX routines
 * 
 * @author Fastily
 *
 */
public final class FXTool
{
	/**
	 * Constructors disallowed
	 */
	private FXTool()
	{
		
	}
	
	/**
	 * Runs an asynchronous Task in a new thread.
	 * @param r The task to run
	 */
	public static void runAsyncTask(Runnable r)
	{
		new Thread(new Task<Integer>() {
			protected Integer call()
			{
				r.run();
				return 0;
			}
		}).start();
	}
	
	/**
	 * Sets some basic parameters and shows a Stage.
	 * @param stg The Stage to show
	 * @param title The Window title
	 * @param sc The Scene to apply to the Stage
	 * @return The stage, <code>stg</code>
	 */
	public static Stage setupAndShowStage(Stage stg, String title, Scene sc)
	{
		stg.setTitle(title);
		stg.setScene(sc );
		stg.show();
		
		return stg;
	}
}