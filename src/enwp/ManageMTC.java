package enwp;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import jwiki.core.MQuery;
import jwiki.core.Wiki;
import jwiki.util.GroupQueue;
import jwikix.util.WTool;
import jwikix.util.WikiGen;

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
	 * The Copy to Wikimedia Commons template title
	 */
	private static final String mtc = "Template:Copy to Wikimedia Commons";

	/**
	 * Creates the regular expression matching Copy to Wikimedia Commons
	 */
	private static final String tRegex = WTool.makeTemplateRegex(wiki, mtc);

	/**
	 * The ncd template to fill out
	 */
	private static final String ncd = String.format("{{Now Commons|%%s|date=%s|bot=%s}}",
			DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now(ZoneId.of("UTC"))), botName);

	/**
	 * Main driver
	 * 
	 * @param args No arguments used
	 */
	public static void main(String[] args)
	{
		GroupQueue<String> l = new GroupQueue<>(wiki.whatTranscludesHere(mtc), 50);

		while (l.has())
			for (Map.Entry<String, ArrayList<String>> e : MQuery.getSharedDuplicatesOf(wiki, l.poll()).entrySet())
			{
				if (e.getValue().isEmpty())
					continue;

				String title = e.getKey();

				if (wiki.getTemplatesOnPage(title).contains("Template:Now Commons"))
					wiki.replaceText(title, tRegex, "BOT: Remove redundant {{Copy to Wikimedia Commons}} tag");
				else
					wiki.edit(title,
							String.format("%s%n%s", String.format(ncd, e.getValue().get(0)), wiki.getPageText(title).replaceAll(tRegex, "")),
							"BOT: Add {{Now Commons}} to request human review because file is available on Commons");
			}
	}
}