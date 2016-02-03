package enwp;

import java.util.ArrayList;

import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.extras.WikiGen;
import jwiki.util.FL;
import util.StrTool;
import util.WTool;

/**
 * Finds files flagged as missing license which seem to have a license template.
 * 
 * @author Fastily
 *
 */
public class NLDReport
{
	/**
	 * Main driver
	 * 
	 * @param args Not used, no args accepted.
	 */
	public static void main(String[] args)
	{
		Wiki wiki = WikiGen.wg.get("FastilyClone", "en.wikipedia.org");
		ArrayList<String> goodlist = FL.toSAL("Category:All free media", "Category:All non-free media");

		ArrayList<String> results = FL.toAL(wiki.getCategoryMembers("Category:Wikipedia files with unknown copyright status")
				.parallelStream().filter(c -> c.matches(".+? \\d{4}")).flatMap(c -> wiki.getCategoryMembers(c, NS.FILE).stream())
				.filter(s -> StrTool.arraysIntersect(goodlist, wiki.getCategoriesOnPage(s))));

		wiki.edit("User:FastilyClone/FixedNLDs", WTool.listify(WPStrings.updatedAt, results, true), "Update report");

	}
}