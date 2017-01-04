package enwp.bots;

import java.util.ArrayList;

import ctools.util.TParse;
import ctools.util.Toolbox;
import ctools.util.WikiX;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;

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
	private static final Wiki wiki = Toolbox.getFastilyBot();

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
		ArrayList<String> l = wiki
				.getCategoryMembers("Category:Wikipedia files with the same name on Wikimedia Commons as of unknown date", NS.FILE);
		l.removeAll(WikiX.getCategoryMembersR(wiki, "Category:Wikipedia files reviewed on Wikimedia Commons").y);

		for (String s : l)
			wiki.replaceText(s, ncRegex, "{{Subst:Ncd}}", "BOT: Dating {{Now Commons}}");
	}
}