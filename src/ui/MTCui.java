package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * A GUI wrapper for tools.MTC
 * 
 * @author Fastily
 * @see tools.MTC
 */
public class MTCui extends Application
{
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
		LoginController t = LoginController.load(getClass());
		t.setOnLoginSuccess(() -> System.out.println("Hello!"));

		// Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));

		primaryStage.setTitle("MTC!");
		primaryStage.setScene(new Scene(t.getRoot()));
		primaryStage.show();

	}
}