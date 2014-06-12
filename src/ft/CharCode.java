package ft;

import jwiki.util.FError;
import jwiki.util.ReadFile;

public class CharCode
{
	public static void main(String[] args)
	{
		if(args.length == 0)
			FError.errAndExit("You didn't specify any args");
		
		String fmt = "eval(String.fromCharCode(%s));%n";
		char[] cl =  new ReadFile(args[0]).getTextAsBlock().trim().toCharArray();
		if(cl.length == 0)
			return;
		else if(cl.length == 1)
			System.out.printf(fmt, "" + (int) cl[0]);
		else
		{
			String x = "" + (int) cl[0];
			for(int i = 1; i < cl.length; i++)
				x += String.format(",%d", (int) cl[i]);
			System.out.printf(fmt, x);
		}
	}
}