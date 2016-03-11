package enwp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jwiki.core.MQuery;
import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.MapList;
import jwikix.util.WTool;
import jwikix.util.WikiGen;

/**
 * Finds files flagged as missing license which seem to have a license template.
 * 
 * @author Fastily
 *
 */
public final class NLDReport
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = WikiGen.wg.get("FastilyClone", "en.wikipedia.org");

	/**
	 * The data set map, which is [ file : category ]
	 */
	private static final HashMap<String, String> ds = initDS();

	/**
	 * Main driver
	 * 
	 * @param args Not used, no args accepted.
	 */
	public static void main(String[] args)
	{
		MapList<String, String> l = new MapList<>();

		for (String s : FL.toAL(MQuery.getCategoriesOnPage(wiki, new ArrayList<>(ds.keySet())).entrySet().stream()
				.filter(e -> catsOk(e.getValue())).map(Map.Entry::getKey)))
			l.put(ds.get(s), s);

		ArrayList<String> keys = new ArrayList<>(l.l.keySet());
		Collections.sort(keys);

		String x = "__NOTOC__\n" + WPStrings.updatedAt;
		for (String s : keys)
			x += WTool.listify(String.format("=== [[:%s]] ===%n", s), l.l.get(s), true);

		wiki.edit("User:FastilyClone/FixedNLDs", x, "Update Report");
	}

	/**
	 * Initializes the dataset, which is a map of [ file : category ]
	 * 
	 * @return The dataset map.
	 */
	private static HashMap<String, String> initDS()
	{
		HashMap<String, String> l = new HashMap<>();
		for (String c : wiki.getCategoryMembers("Category:Wikipedia files with unknown copyright status"))
			if (c.matches(".+? " + WPStrings.DMYRegex))
				for (String s : wiki.getCategoryMembers(c, NS.FILE))
					l.put(s, c);

		return l;
	}

	/**
	 * Checks each file in a daily nld category for eligibility and reports the result.
	 * 
	 * @param cat The category to check
	 * @return A report for the category, as a String.
	 */
	private static boolean catsOk(ArrayList<String> cats)
	{
		return cats.contains("Category:All free media") || cats.contains("Category:All non-free media");
	}
}