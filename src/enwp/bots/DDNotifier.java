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
import java.util.Map;
import java.util.Optional;

import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.MapList;
import jwiki.util.Tuple;
import jwikix.core.WikiX;
import util.Toolbox;
import util.WPStrings;

/**
 * Checks daily deletion categories on enwp and notifies users if they have not been notified.
 * 
 * @author Fastily
 *
 */
public class DDNotifier
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * A list of categories to check if users have been notified.
	 */
	private static final ArrayList<Tuple<String, String>> rules = FL
			.mapToList(Toolbox.fetchPairedConfig(wiki, "User:FastilyBot/Task6Rules"));

	/**
	 * The start of today, and the start of yesterday (target date)
	 */
	private static final ZonedDateTime targetDT = ZonedDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.of(0, 0), ZoneOffset.UTC).minusDays(1);

	/**
	 * Time stamps, used to select talk page revisions which were made in the past day only.
	 */
	private static final Instant start = Instant.from(targetDT), end = Instant.now();

	/**
	 * The dated-category suffix used to select daily deletion categories to process.
	 */
	private static final String targetDateStr = String.format("%d %s %d", targetDT.getDayOfMonth(),
			targetDT.getMonth().getDisplayName(TextStyle.FULL, Locale.US), targetDT.getYear());

	/**
	 * The title blacklist; the bot will not edit any page transcluding {{bots}}
	 */
	private static final HashSet<String> talkPageBL = Toolbox.fetchNoBots(wiki);

	/**
	 * The list of files with templates that trigger the bot unnecessarily.
	 */
	private static final HashSet<String> idkL = FL
			.toSet(FL.toSAL("Template:Don't know", "Template:Somewebsite", "Template:Untagged", "Template:No copyright holder",
					"Template:No copyright information").stream().flatMap(s -> wiki.whatTranscludesHere(s).stream()));

	/**
	 * Main driver
	 * 
	 * @param args Program arguments; not used.
	 */
	public static void main(String[] args)
	{
		for (Tuple<String, String> t : rules)
			procPair(t.x, t.y);
	}

	/**
	 * Processes a rules-set pair.
	 * 
	 * @param rootCat The root category to look for applicable categories in
	 * @param templ The Template which will be used to notify users.
	 */
	private static void procPair(String rootCat, String templ)
	{
		Optional<String> cat = wiki.getCategoryMembers(rootCat, NS.CATEGORY).stream().filter(s -> s.endsWith(targetDateStr)).findAny();
		if (!cat.isPresent())
			return;

		MapList<String, String> ml = new MapList<>();
		for (String s : wiki.getCategoryMembers(cat.get(), NS.FILE))
			if (!idkL.contains(s))
				ml.put(WikiX.getPageAuthor(wiki, s), s); // null keys allowed

		for (Map.Entry<String, ArrayList<String>> e : ml.l.entrySet())
		{
			String tp = "User talk:" + e.getKey();
			if (talkPageBL.contains(tp))
				continue;

			ArrayList<String> notifyList = Toolbox.detLinksInHist(wiki, tp, e.getValue(), start, end);
			if (notifyList.isEmpty())
				continue;

			String x = String.format("%n{{subst:%s|1=%s}}%n", templ, notifyList.get(0));
			if (notifyList.size() > 1)
				x += Toolbox.listify("\nAlso:\n", notifyList.subList(1, notifyList.size()), true);
			
			wiki.addText(tp, x + WPStrings.botNote, "BOT: Notify user of possible file issue(s)", false);
		}
	}
}