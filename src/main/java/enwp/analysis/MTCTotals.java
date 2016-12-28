package enwp.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ctools.util.Toolbox;
import enwp.WPStrings;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.Tuple;

/**
 * Report which totals MTC counts by user.  Truncated to first 500 entries max in MTC.
 * 
 * @author Fastily
 *
 */
public class MTCTotals
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * The MtC category to inspect.
	 */
	private static String mtcCat = "Category:Copy to Wikimedia Commons";
	
	/**
	 * Main driver
	 * 
	 * @param args Program arguments. Takes the source category to analyze as the only argument.
	 */
	public static void main(String[] args)
	{
		if (args.length != 0)
			mtcCat = args[0];

		HashSet<String> fileCache = new HashSet<>(), userCache = new HashSet<>();
		HashMap<String, Integer> m = new HashMap<>();
		
		ArrayList<String> mtcList = wiki.getCategoryMembers(mtcCat, NS.FILE);
		String user;
		for (int i = 0; i < 500; i++)
		{
			System.err.printf("Processing item %d%n", i);
			
			if (!fileCache.contains(mtcList.get(i)) && !userCache.contains(user = wiki.getRevisions(mtcList.get(i), 1, true, null, null).get(0).user))
				try
				{
					ArrayList<String> l = wiki.getUserUploads(user);
					fileCache.addAll(l);
					userCache.add(user);
					m.put(user, l.size());
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
		}

		int cnt = 1;
		String dump = WPStrings.updatedAt
				+ "\n{| class=\"wikitable sortable\" style=\"margin-left: auto; margin-right: auto;width:80%;\" \n! # !! User !! Uploads \n";

		for (Tuple<String, Integer> e : FL.mapToList(m))
			dump += String.format("|-%n|%d ||[[Special:ListFiles/%s|%s]] ||%s%n", cnt++, e.x, e.x, e.y);

		dump += "|}";

		wiki.edit("User:FastilyBot/Sandbox1", dump, "Update report");

	}
}