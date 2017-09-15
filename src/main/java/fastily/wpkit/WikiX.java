package fastily.wpkit;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;

/**
 * Miscellaneous Wiki-related routines.
 * 
 * @author Fastily
 *
 */
public final class WikiX
{
	/**
	 * A date formatter for UTC times.
	 */
	public static final DateTimeFormatter iso8601dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * Constructors disallowed
	 */
	private WikiX()
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
		HashMap<String, String> l = new HashMap<>();
		MQuery.getSharedDuplicatesOf(wiki, titles).forEach((k, v) -> {
			if (!v.isEmpty())
				l.put(k, v.get(0));
		});

		return l;
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