package enwp.bots;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.MapList;
import jwiki.util.Tuple;
import jwikix.util.WikiGen;

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
	private static final Wiki wiki = WikiGen.wg.get("FastilyBot", "en.wikipedia.org");

	/**
	 * A list of categories to check if users have been notified.
	 */
	private static final ArrayList<Tuple<String, String>> rules = FL
			.toAL(Arrays.asList(wiki.getPageText("User:FastilyBot/Task6Rules").split("\n")).stream()
					.filter(s -> !s.startsWith("<") && !s.isEmpty()).map(s -> s.split(";")).map(a -> new Tuple<>(a[0], a[1])));

	/**
	 * The start of today, and the start of yesterday (target date)
	 */
	private static final ZonedDateTime targetDT = ZonedDateTime.now(ZoneId.of("UTC")).withHour(0).withMinute(0).withSecond(0)
			.withNano(0).minusDays(1);

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
	private static final HashSet<String> talkPageBL = new HashSet<>(wiki.whatTranscludesHere("Template:Bots"));

	/**
	 * The list of files with templates that trigger the bot unnecessarily.
	 */
	private static final HashSet<String> idkL = initIdk();

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
		String cat = fetchCat(rootCat);
		if (cat == null)
			return;

		MapList<String, String> ml = new MapList<>();
		for (String s : wiki.getCategoryMembers(cat, NS.FILE))
			if (!idkL.contains(s))
				try
				{
					ml.put(wiki.getRevisions(s, 1, true, null, null).get(0).user, s);
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}

		for (Map.Entry<String, ArrayList<String>> e : ml.l.entrySet())
		{
			String talkpage = "User talk:" + e.getKey();
			if (talkPageBL.contains(talkpage))
				continue;

			ArrayList<String> notifyList = testNotifiedFor(talkpage, e.getValue());
			if (notifyList.isEmpty())
				continue;

			wiki.addText(talkpage, generateMessage(notifyList, templ), "BOT: Notify user of possible file issue(s)", false);
		}
	}

	/**
	 * Generates a message which will be sent to a user
	 * 
	 * @param l The list of titles to use
	 * @param templ The base message Template.
	 * @return The message, as a String.
	 */
	private static String generateMessage(ArrayList<String> l, String templ)
	{
		String x = String.format("%n{{subst:%s|1=%s}}%n", templ, l.get(0));

		if (l.size() > 1)
		{
			x += "\nAlso:\n";
			for (int i = 1; i < l.size(); i++)
				x += String.format("* [[:%s]]%n", l.get(i));
		}

		return x
				+ "\n<span style=\"color:red;font-weight:bold;\">ATTENTION</span>: This is an automated, [[Wikipedia:Bots|BOT]]-generated message.  "
				+ "This bot DID NOT nominate your file(s) for deletion; please refer to the [[Help:Page history|page history]] of each individual file "
				+ "for details. Thanks, ~~~~";
	}

	/**
	 * Test if a user has been notified about a File in the past calendar day.
	 * 
	 * @param userTalk The user's talk page, to be checked for links to the specified files in <code>l</code>
	 * @param l The list of files to check for on <code>userTakl</code>.
	 * @return A list of files that the user should be notified about.
	 */
	private static ArrayList<String> testNotifiedFor(String userTalk, ArrayList<String> l)
	{
		ArrayList<String> texts = FL.toAL(wiki.getRevisions(userTalk, -1, false, start, end).stream().map(r -> r.text));
		return FL.toAL(l.stream().filter(s -> texts.stream().noneMatch(t -> t.matches("(?si).*?\\[\\[:(\\Q" + s + "\\E)\\]\\].*?"))));
	}

	/**
	 * Fetches an applicable category title from root categories.
	 * 
	 * @param rootCat The root category to check
	 * @return Yesterday's daily deletion category for the specified <code>rootCat</code>, or null if it doesn't exist.
	 */
	private static String fetchCat(String rootCat)
	{
		for (String s : wiki.getCategoryMembers(rootCat, NS.CATEGORY))
			if (s.endsWith(targetDateStr))
				return s;

		return null;
	}

	/**
	 * Initializes the list of files which transclude templates that trigger the bot unnecessarily.
	 * 
	 * @return The list of files with templates that trigger the bot unnecessarily.
	 */
	private static HashSet<String> initIdk()
	{
		HashSet<String> l = new HashSet<>();
		for (String s : FL.toSAL("Template:Don't know", "Template:Somewebsite", "Template:Untagged", "Template:No copyright holder",
				"Template:No copyright information"))
			l.addAll(wiki.whatTranscludesHere(s));

		return l;
	}
}