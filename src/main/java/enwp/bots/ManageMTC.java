package enwp.bots;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;

import ctools.util.Toolbox;
import ctools.util.WikiX;
import enwp.WTP;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.GroupQueue;

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
		ArrayList<String> tl = wiki.whatTranscludesHere(WTP.mtc.title);
		tl.removeAll(WTP.nobots.getTransclusionSet(wiki, NS.FILE));
		
		GroupQueue<String> l = new GroupQueue<>(tl, 50);
		while (l.has())
			WikiX.getFirstOnlySharedDuplicate(wiki, l.poll()).forEach((k,v) ->
			{
				if (nowCommons.contains(k))
					wiki.replaceText(k, tRegex, "BOT: File has already been copied to Commons");
				else
					wiki.edit(k, String.format(ncd, v) + wiki.getPageText(k).replaceAll(tRegex, ""),
							"BOT: File is available on Commons");
			});
	}
}