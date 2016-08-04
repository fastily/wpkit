package enwp.bots;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.GroupQueue;
import jwiki.util.Tuple;
import jwikix.core.MQueryX;
import jwikix.core.TParse;
import util.Toolbox;

/**
 * Bot which finds files on enwp which have been copied to Commons and tags them for human review.
 * 
 * @author Fastily
 *
 */
public final class ManageMTC
{
	/**
	 * The Wiki Object used for the bot.
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
	 * The list of pages transcluding {{Now Commons}}
	 */
	private static final HashSet<String> nowCommons = new HashSet<>(wiki.whatTranscludesHere("Template:Now Commons"));

	/**
	 * The list of pages for bots to avoid
	 */
	private static final HashSet<String> nobots = new HashSet<>(wiki.whatTranscludesHere("Template:Bots"));
	
	/**
	 * The ncd template to fill out
	 */
	protected static final String ncd = String.format("{{Now Commons|%%s|date=%s|bot=%s}}%n",
			DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now(ZoneId.of("UTC"))), "FastilyBot");

	/**
	 * Main driver
	 * 
	 * @param args No arguments used
	 */
	public static void main(String[] args)
	{
		GroupQueue<String> l = new GroupQueue<>(wiki.whatTranscludesHere(mtc), 50);

		while (l.has())
			for (Tuple<String, String> e : FL.mapToList(MQueryX.getFirstOnlySharedDuplicate(wiki, l.poll())))
				if (nobots.contains(e.x))
					continue;
				else if (nowCommons.contains(e.x))
					wiki.replaceText(e.x, tRegex, "BOT: Remove redundant {{Copy to Wikimedia Commons}}");
				else
					wiki.edit(e.x, String.format(ncd, e.y) + wiki.getPageText(e.x).replaceAll(tRegex, ""),
							"BOT: Add {{Now Commons}}, file is available on Commons");
	}
}