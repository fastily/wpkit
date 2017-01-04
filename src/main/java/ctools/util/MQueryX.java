package ctools.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;

/**
 * Static methods wrapping and extending MQuery functions.
 * 
 * @author Fastily
 *
 */
public final class MQueryX
{
	/**
	 * Constructors disallowed
	 */
	private MQueryX()
	{

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
		return FL.toHM(MQuery.getSharedDuplicatesOf(wiki, titles).entrySet().stream().filter(e -> !e.getValue().isEmpty()),
				Map.Entry::getKey, e -> e.getValue().get(0));
	}
}