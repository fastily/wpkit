package enwp;

import java.util.ArrayList;

import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.WikiGen;
import util.WTool;

/**
 * Removes Copy to Wikimedia Commons on enwp files that may be ineligible for transfer to Commons.
 * 
 * @author Fastily
 *
 */
public final class RemoveBadMTC
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = WikiGen.wg.get("FastilyBot", "en.wikipedia.org");

	/**
	 * The Copy to Wikimedia Commons template title
	 */
	private static final String mtc = "Template:Copy to Wikimedia Commons";
	
	/**
	 * Creates the regular expression matching Copy to Wikimedia Commons
	 */
	private static final String tRegex = WTool.makeTRegex(wiki, mtc);

	/**
	 * The list of files transcluding Copy to Wikimedia Commons.
	 */
	private static final ArrayList<String> mtcFiles = wiki.whatTranscludesHere(mtc);

	/**
	 * Main driver
	 * 
	 * @param args No args, not used.
	 */
	public static void main(String[] args)
	{
		// ignore files flagged by humans
		mtcFiles.removeAll(WTool.getCategoryMembersR(wiki, "Category:Copy to Wikimedia Commons reviewed by a human").y);
		
		ArrayList<String> fails = new ArrayList<>();

		for (String blt : wiki.getLinksOnPage("User:FastilyBot/Task2Blacklist"))
			for (String x : FL.toAL(wiki.whatTranscludesHere(blt).parallelStream().filter(mtcFiles::contains)))
			{
				String oText = wiki.getPageText(x);
				String newText = oText.replaceAll(tRegex, "");

				if (oText.equals(newText))
					fails.add(x);
				else
					wiki.edit(x, newText, "BOT: Remove {{Copy to Wikimedia Commons}}; the file may not be eligible for Commons");
			}

		wiki.edit("User:FastilyBot/Task2Borked", WTool.listify(WPStrings.updatedAt, fails, true), "Update list");
	}
}