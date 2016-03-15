package enwp;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jwiki.core.MQuery;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.GroupQueue;
import jwiki.util.Tuple;
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
	private static final String ncd = String.format("{{Now Commons|%%s|date=%s|bot=%s}}%n",
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
		{
			HashMap<String, String> dupes = new HashMap<>(MQuery.getSharedDuplicatesOf(wiki, l.poll()).entrySet().stream()
					.filter(e -> !e.getValue().isEmpty()).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0))));
			ArrayList<String> tempL = FL.toAL(MQuery.getTemplatesOnPage(wiki, new ArrayList<>(dupes.keySet())).entrySet().stream()
					.filter(e -> e.getValue().contains("Template:Now Commons")).map(Map.Entry::getKey));

			for (Tuple<String, String> e : FL.mapToList(dupes))
				if (tempL.contains(e.x))
					wiki.replaceText(e.x, tRegex, "BOT: Remove redundant {{Copy to Wikimedia Commons}} tag");
				else
					wiki.edit(e.x, String.format(ncd, e.y) + wiki.getPageText(e.x).replaceAll(tRegex, ""),
							"BOT: Add {{Now Commons}} to request human review because file is available on Commons");
		}
	}
}