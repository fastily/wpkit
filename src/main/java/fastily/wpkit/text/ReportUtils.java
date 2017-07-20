package fastily.wpkit.text;

import java.time.format.DateTimeFormatter;
import java.util.Collection;

import fastily.jwiki.util.FSystem;

/**
 * Report-related utilities
 * 
 * @author Fastily
 *
 */
public class ReportUtils
{
	/**
	 * A date formatter for UTC times.
	 */
	public static final DateTimeFormatter iso8601dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * Constructors disallowed
	 */
	private ReportUtils()
	{

	}

	/**
	 * Generates a Wiki-text ready, wiki-linked, unordered list from a list of titles.
	 * 
	 * @param header A header/lead string to apply at the beginning of the returned String.
	 * @param titles The titles to use
	 * @param doEscape Set as true to escape titles. i.e. adds a {@code :} before each link so that files and
	 *           categories are properly escaped and appear as links.
	 * @return A String with the titles as a linked, unordered list, in Wiki-text.
	 */
	public static String listify(String header, Collection<String> titles, boolean doEscape)
	{
		String fmtStr = "* [[" + (doEscape ? ":" : "") + "%s]]" + FSystem.lsep;

		String x = "" + header;
		for (String s : titles)
			x += String.format(fmtStr, s);

		return x;
	}
}