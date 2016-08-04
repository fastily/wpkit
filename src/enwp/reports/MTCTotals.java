package enwp.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.Tuple;
import util.Toolbox;

/**
 * Report which totals MTC counts by user
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

		int i = 0;
		String user;
		for (String s : wiki.getCategoryMembers(mtcCat, NS.FILE))
		{
			System.err.printf("Processing item %d%n", ++i);
			
			if (!fileCache.contains(s) && !userCache.contains(user = wiki.getRevisions(s, 1, true, null, null).get(0).user))
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

		ArrayList<Tuple<String, Integer>> l = FL.mapToList(m);
		Collections.sort(l, (o1, o2) -> o2.y.compareTo(o1.y)); // G -> L

		String x = "Updated ~~~~~\n\n";
		for (Tuple<String, Integer> e : l)
			if (e.y >= 5)
				x += String.format("# [[Special:ListFiles/%s|%s]] - %d%n", e.x, e.x, e.y);

		wiki.edit("User:FastilyBot/Sandbox1", x, "Update report");

	}
}