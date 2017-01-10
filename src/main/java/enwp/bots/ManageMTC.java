package enwp.bots;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import ctools.util.Toolbox;
import ctools.util.WikiX;
import enwp.WTP;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.GroupQueue;
import fastily.jwiki.util.Tuple;

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
	 * Creates the regular expression matching Copy to Wikimedia Commons
	 */
	private static final String tRegex = WTP.mtc.getRegex(wiki);

	/**
	 * The list of pages transcluding {{Now Commons}}
	 */
	private static final HashSet<String> nowCommons = WTP.ncd.getTransclusionSet(wiki, NS.FILE);

	/**
	 * The list of pages for bots to avoid
	 */
	private static final HashSet<String> nobots = WTP.nobots.getTransclusionSet(wiki, NS.FILE);
	
	/**
	 * The ncd template to fill out
	 */
	protected static final String ncd = String.format("{{Now Commons|%%s|date=%s|bot=%s}}%n",
			DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now(ZoneId.of("UTC"))), wiki.whoami());

	/**
	 * Main driver
	 * 
	 * @param args No arguments used
	 */
	public static void main(String[] args)
	{
		GroupQueue<String> l = new GroupQueue<>(wiki.whatTranscludesHere(WTP.mtc.title), 50);

		while (l.has())
			for (Tuple<String, String> e : FL.mapToList(WikiX.getFirstOnlySharedDuplicate(wiki, l.poll())))
				if (nobots.contains(e.x))
					continue;
				else if (nowCommons.contains(e.x))
					wiki.replaceText(e.x, tRegex, "BOT: Remove redundant {{Copy to Wikimedia Commons}}");
				else
					wiki.edit(e.x, String.format(ncd, e.y) + wiki.getPageText(e.x).replaceAll(tRegex, ""),
							"BOT: Add {{Now Commons}}, file is available on Commons");
	}
}