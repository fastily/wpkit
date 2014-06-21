package ft;

import java.util.ArrayList;

import jwiki.commons.CStrings;
import jwiki.core.Wiki;
import jwiki.mbot.WAction;
import jwiki.util.WikiGen;

public class RedirectClear
{
	private static Wiki wiki = WikiGen.generate("Fastily");
	
	public static void main(String[] args)
	{
		int process = 250;
		for (String s : args)
			if (s.matches("\\d*?"))
				process = Integer.parseInt(s);
		
		ArrayList<RItem> l = new ArrayList<RItem>();
		for (String s : wiki.allPages(null, true, process, "File"))
			l.add(new RItem(s));
		
		WikiGen.genM("Fastily", 6).start(l.toArray(new RItem[0]));
		
	}
	
	private static class RItem extends WAction
	{
		private RItem(String title)
		{
			super(title, null, null);
		}
		
		public boolean doJob(Wiki wiki)
		{
			if (!wiki.globalUsage(title).isEmpty())
				return true;
			
			//TODO: This really should be a replacement operation
			if(wiki.imageUsage(title).length > 0)
				return true;

			return wiki.delete(title, CStrings.house);
		}
	}
}