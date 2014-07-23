package sand;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Derp
{
	public static void main(String[] args)
	{
		Logger l = Logger.getLogger("lol");
		l.info("This is a test");
		
		//for(int i = 0; i < 10 ; i++)
		//	l.severe("Oh no: " + i);
		
		
		
		
		l.log(Level.INFO, "This is a info message");
		
		
		l.logp(Level.INFO, "main()", "inner()", "woohoo");
	}
}