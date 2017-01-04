package ctools.util;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
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
	 * Get the page author of a title. This is based on the first available public revision to a page.
	 * 
	 * @param wiki The Wiki object to use
	 * @param title The title to query
	 * @return The page author (without the "User:" prefix), or null on error.
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
}