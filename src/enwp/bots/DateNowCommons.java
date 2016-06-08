package enwp.bots;

import jwiki.core.NS;
import jwiki.core.Wiki;
import jwikix.util.TParse;
import jwikix.util.WikiGen;

/**
 * Fills in date parameter (and other missing parameters) for files in [[Category:Wikipedia files with the same name on
 * Wikimedia Commons as of unknown date]].
 * 
 * @author Fastily
 *
 */
public class DateNowCommons
{
	/**
	 * The Wiki Object to use
	 */
	private static final Wiki wiki = WikiGen.wg.get("FastilyBot", "en.wikipedia.org");

	/**
	 * Matches {{Now Commons}} templates
	 */
	private static final String ncRegex = TParse.makeTemplateRegex(wiki, "Template:Now Commons");

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used
	 */
	public static void main(String[] args)
	{
		for (String s : wiki.getCategoryMembers("Category:Wikipedia files with the same name on Wikimedia Commons as of unknown date",
				NS.FILE))
			wiki.replaceText(s, ncRegex, "{{Subst:Ncd}}", "BOT: Dating {{Now Commons}}");
	}
}