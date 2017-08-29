package fastily.wpkit.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.Triple;
import fastily.jwiki.util.Tuple;

/**
 * Miscellaneous Wiki-related routines.
 * 
 * @author Fastily
 *
 */
public final class WikiX
{
	/**
	 * Constructors disallowed
	 */
	private WikiX()
	{

	}

	/**
	 * Strips namespaces from a list of titles for a given Wiki.
	 * 
	 * @param wiki The wiki object to use
	 * @param l The titles which will have their namespace prefixes removed by <code>wiki</code>.
	 * @return A new list of titles, with their namespace prefixes removed.
	 */
	public static ArrayList<String> stripNamespaces(Wiki wiki, ArrayList<String> l)
	{
		return FL.toAL(l.stream().map(wiki::nss));
	}

	/**
	 * Recursively searches a category for members.
	 * 
	 * @param wiki The Wiki object to use
	 * @param root The root/parent category to start searching in
	 * @return A Tuple in the form: ( categories visited, members found )
	 */
	public static Tuple<HashSet<String>, HashSet<String>> getCategoryMembersR(Wiki wiki, String root)
	{
		HashSet<String> seen = new HashSet<>(), l = new HashSet<>();
		getCategoryMembersR(wiki, root, seen, l);

		return new Tuple<>(seen, l);
	}

	/**
	 * Recursively searches a category for members.
	 * 
	 * @param wiki The Wiki object to use
	 * @param root The root/parent category to start searching in
	 * @param seen Lists the categories visited. Tracking this avoids circular self-categorizing categories.
	 * @param l Lists the category members encountered.
	 */
	private static void getCategoryMembersR(Wiki wiki, String root, HashSet<String> seen, HashSet<String> l)
	{
		seen.add(root);

		ArrayList<String> results = wiki.getCategoryMembers(root);
		ArrayList<String> cats = wiki.filterByNS(results, NS.CATEGORY);

		results.removeAll(cats); // cats go in seen
		l.addAll(results);

		for (String s : cats)
		{
			if (seen.contains(s))
				continue;

			getCategoryMembersR(wiki, s, seen, l);
		}
	}

	/**
	 * Get the page author of a page. This is based on the first available public revision to a page.
	 * 
	 * @param wiki The Wiki object to use
	 * @param title The title to query
	 * @return The page author, without the {@code User:} prefix, or null on error.
	 */
	public static String getPageAuthor(Wiki wiki, String title)
	{
		try
		{
			return wiki.getRevisions(title, 1, true, null, null).get(0).user;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the most recent editor of a page.
	 * 
	 * @param wiki The Wiki object to use
	 * @param title The title to query
	 * @return The most recent editor to a page, without the {@code User:} prefix, or null on error.
	 */
	public static String getLastEditor(Wiki wiki, String title)
	{
		try
		{
			return wiki.getRevisions(title, 1, false, null, null).get(0).user;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the first shared (non-local) duplicate for each file with a duplicate. Filters out files which do not have
	 * duplicates.
	 * 
	 * @param wiki The Wiki object to use
	 * @param titles The titles to get duplicates for
	 * @return A Map where each key is the original, and each value is the first duplicate found.
	 */
	public static HashMap<String, String> getFirstOnlySharedDuplicate(Wiki wiki, ArrayList<String> titles)
	{
		HashMap<String, String> l = new HashMap<>();
		MQuery.getSharedDuplicatesOf(wiki, titles).forEach((k, v) -> {
			if (!v.isEmpty())
				l.put(k, v.get(0));
		});

		return l;
	}

	/**
	 * Lists section headers on a page. This method takes inputs from {@code getSectionHeaders()} and
	 * {@code getPageText()}
	 * 
	 * @param sectionData A response from {@code getSectionHeaders()}.
	 * @param text The text from the same page, via {@code getPageText()}
	 * @return A List with a Triple containing [ Header Level , Header Title, The Full Header and Section Text ]
	 */
	public static ArrayList<Triple<Integer, String, String>> listPageSections(ArrayList<Triple<Integer, String, Integer>> sectionData,
			String text)
	{
		ArrayList<Triple<Integer, String, String>> results = new ArrayList<>();

		if (sectionData.isEmpty())
			return results;

		Triple<Integer, String, Integer> curr;
		for (int i = 0; i < sectionData.size() - 1; i++)
		{
			curr = sectionData.get(i);
			results.add(new Triple<>(curr.x, curr.y, text.substring(curr.z, sectionData.get(i + 1).z)));
		}

		curr = sectionData.get(sectionData.size() - 1);
		results.add(new Triple<>(curr.x, curr.y, text.substring(curr.z)));

		return results;
	}

	/**
	 * Determine if a set of link(s) has existed on a page over a given time period.
	 * 
	 * @param wiki The Wiki object to use
	 * @param title The title to query
	 * @param l The list of link(s) to look for in the history of <code>title</code>.
	 * @param start The time to start looking at (inclusive). Optional - set null to disable.
	 * @param end The time to stop the search at (exclusive). Optional - set null to disable.
	 * @return A list of link(s) that were found at some point in the page's history.
	 */
	public static ArrayList<String> detLinksInHist(Wiki wiki, String title, ArrayList<String> l, Instant start, Instant end)
	{
		ArrayList<String> texts = FL.toAL(wiki.getRevisions(title, -1, false, start, end).stream().map(r -> r.text));
		return FL.toAL(l.stream().filter(s -> texts.stream().noneMatch(t -> t.matches("(?si).*?\\[\\[:??(\\Q" + s + "\\E)\\]\\].*?"))));
	}
	
	/**
	 * Generate a regex matching filenames of files that may be uploaded to {@code wiki}.  WARNING: this method is not cached, so save the result!
	 * @param wiki The Wiki object to use
	 * @return The regex.
	 */
	public static String allowedFileExtsRegex(Wiki wiki)
	{
		return "(?i).+?\\.(" + FL.pipeFence(wiki.getAllowedFileExts()) + ")";
	}
}