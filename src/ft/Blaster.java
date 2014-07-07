package ft;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.security.auth.login.LoginException;

import jwiki.core.Settings;
import jwiki.core.Wiki;
import jwiki.util.FIO;
import jwiki.util.FString;

public class Blaster
{
	public static void main(String[] args) throws LoginException, InterruptedException
	{
		Settings.useragent = "Ughasdlf0923";
		Wiki wiki = new Wiki(args[0], args[1]);
		ConcurrentLinkedQueue<String> fails = new ConcurrentLinkedQueue<String>();

		ArrayList<Thread> l = new ArrayList<Thread>();
		for (Path f : FIO.findFiles(Paths.get(args[2])))
			l.add(new Thread(() -> {
				if (!wiki.upload(f, "File:" + makeName(f), "{{subst:Nld}}", ""))
					fails.add(FIO.getFileName(f));
			}));

		for (Thread t : l)
			t.start();

		for (Thread t : l)
			t.join();
		
		if(!fails.isEmpty())
		{
			System.out.printf("Failed (%d):%n", fails.size());
			for(String s : fails)
				System.out.println(s);
		}
	}

	protected static String makeName(Path f)
	{
		String x = "";
		int i = 0;
		for (char c : FIO.getFileName(f).toCharArray())
		{
			x += (char) (c % 26 + 97);
			if(++i > 10)
				break;
		}
		return FString.capitalize(x + FIO.getExtension(f, true));
	}
}