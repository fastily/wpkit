package enwp.bots;

import java.util.HashSet;

import ctools.util.Toolbox;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwikix.core.TParse;
import fastily.jwikix.core.WikiX;

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
	private static final Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * The Copy to Wikimedia Commons template title
	 */
	private static final String mtc = "Template:Copy to Wikimedia Commons";
	
	/**
	 * Creates the regular expression matching Copy to Wikimedia Commons
	 */
	private static final String tRegex = TParse.makeTemplateRegex(wiki, mtc);

	/**
	 * Main driver
	 * 
	 * @param args No args, not used.
	 */
	public static void main(String[] args) throws Throwable
	{
		HashSet<String> mtcFiles = new HashSet<>(wiki.filterByNS(wiki.whatTranscludesHere(mtc), NS.FILE));
		mtcFiles.removeAll(WikiX.getCategoryMembersR(wiki, "Category:Copy to Wikimedia Commons reviewed by a human").y); //ignore reviewed files
		
		for (String blt : wiki.getLinksOnPage("User:FastilyBot/Task2Blacklist"))
			for (String x : FL.toAL(wiki.getCategoryMembers(blt, NS.FILE).stream().filter(mtcFiles::contains)))
				wiki.replaceText(x, tRegex, "", "BOT: Remove {{Copy to Wikimedia Commons}}; the file may not be eligible for Commons");
	}
}