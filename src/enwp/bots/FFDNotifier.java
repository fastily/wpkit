package enwp.bots;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.MapList;
import jwiki.util.Tuple;
import jwikix.core.WikiX;
import util.Toolbox;
import util.WPStrings;

/**
 * Leaves courtesy notifications (where possible) for users whose files were nominated at FfD.
 * 
 * @author Fastily
 *
 */
public final class FFDNotifier
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * The start of today
	 */
	private static final ZonedDateTime today = ZonedDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.of(0, 0), ZoneOffset.UTC);

	/**
	 * Instants for the start of today and the current time (end)
	 */
	private static final Instant start = today.toInstant(), end = Instant.now();

	/**
	 * The title of today's FfD
	 */
	private static String targetFFD = String.format("Wikipedia:Files for discussion/%d %s %d", today.getYear(),
			today.getMonth().getDisplayName(TextStyle.FULL, Locale.US), today.getDayOfMonth());

	/**
	 * The list of pages transcluding {{Bots}}. These are avoided.
	 */
	private static final HashSet<String> noBots = Toolbox.fetchNoBots(wiki);

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		MapList<String, String> l = new MapList<>();
		wiki.getSectionHeaders(targetFFD).stream().filter(t -> t.x == 4 && wiki.whichNS(t.y).equals(NS.FILE))
				.forEach(t -> l.put(WikiX.getPageAuthor(wiki, t.y), t.y));

		for (Tuple<String, ArrayList<String>> e : FL.mapToList(l.l))
		{
			String tp = "User talk:" + e.x;
			if (noBots.contains(tp))
				continue;

			ArrayList<String> rl = Toolbox.detLinksInHist(wiki, tp, e.y, start, end);
			if (rl.isEmpty())
				continue;

			String x = String.format("%n{{subst:fdw|%s}}", wiki.nss(rl.get(0)));
			if (rl.size() > 1)
				x += Toolbox.listify("\nAlso:\n", rl.subList(1, rl.size()), true);
			wiki.addText(tp, x + WPStrings.notBotNom, "BOT: Notify user of FfD", false);
		}
	}
}