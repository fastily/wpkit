package enwp;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import jwiki.core.MQuery;
import jwiki.core.Wiki;
import jwiki.extras.WikiGen;

import util.StrTool;

/**
 * Bot which finds files on enwp which have been copied to Commons and tags them for human review.
 * 
 * @author Fastily
 *
 */
public final class ManageMTC
{
	/**
	 * The username of the bot to use
	 */
	private static final String botName = "FastilyBot";

	/**
	 * The Wiki Object used for the bot.
	 */
	private static final Wiki wiki = WikiGen.wg.get(botName, "en.wikipedia.org");

	/**
	 * Creates the regular expression matching Copy to Wikimedia Commons
	 */
	private static final String tRegex = initTRegex();

	/**
	 * The Copy to Wikimedia Commons template title
	 */
	private static final String mtc = "Template:Copy to Wikimedia Commons";

	/**
	 * Main driver
	 * 
	 * @param args No arguments used
	 */
	public static void main(String[] args)
	{
		String ncd = String.format("{{Now Commons|%%s|date=%s|bot=%s}}",
				DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now(ZoneId.of("UTC"))), botName);

		int i = 0;

		for (Map.Entry<String, ArrayList<String>> e : MQuery.getSharedDuplicatesOf(wiki, wiki.whatTranscludesHere(mtc)).entrySet())
			if (!e.getValue().isEmpty())
				try
				{
					if (i++ > 49) // trial
						break;

					String title = e.getKey();

					if (wiki.getTemplatesOnPage(title).contains("Template:Now Commons"))
						wiki.replaceText(title, tRegex, "BOT: Remove redundant {{Copy to Wikimedia Commons}} tag");
					else
						wiki.edit(title,
								String.format("%s%n%s", String.format(ncd, e.getValue().get(0)),
										wiki.getPageText(title).replaceAll(tRegex, "")),
								"BOT: Add {{Now Commons}} to request human review because file is available at Commons");
				}
				catch (Throwable t)
				{
					t.printStackTrace();
				}
	}

	/**
	 * Constructs a regular expression matching Move To Commons templates.
	 * 
	 * @return A regex matching Move to Commons templates
	 */
	private static String initTRegex()
	{
		ArrayList<String> l = wiki.whatLinksHere(mtc, true);
		l.add(wiki.nss(mtc));

		return StrTool.makeTemplateRegex(StrTool.stripNamespaces(wiki, l));
	}
}