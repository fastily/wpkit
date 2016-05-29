package ui;

import javafx.concurrent.Task;

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
	protected static void runAsyncTask(Runnable r)
	{
		new Thread(new Task<Integer>() {
			protected Integer call()
			{
				r.run();
				return 0;
			}
		}).start();
	}
}