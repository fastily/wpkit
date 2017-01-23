package ctools.util;

import java.util.ArrayList;
import java.util.Collection;

import fastily.jwiki.util.FL;

/**
 * Miscellaneous Wiki-related String processing/parsing methods.
 * 
 * @author Fastily
 *
 */
public final class StrTool
{
	/**
	 * Constructors disallowed
	 */
	private StrTool()
	{

	}
	
	/**
	 * Determines if a List of String contains an element prefixed by {@code prefix}.
	 * @param l The List of String to check.
	 * @param prefix The prefix to look for.
	 * @return True if {@code l} has at least one String prefixed by {@code prefix}.
	 */
	public static boolean hasStrWithPrefix(ArrayList<String> l, String prefix)
	{
		return l.stream().anyMatch(s -> s.startsWith(prefix));
	}

	/**
	 * Filters Strings with a certain list of prefixes from an ArrayList.
	 * 
	 * @param l The ArrayList of source Strings.
	 * @param prefixes Any String in <code>l</code> will be omitted from the final result.
	 * @return A new ArrayList of Strings without the specified prefixes.
	 */
	public static ArrayList<String> omitStrWithPrefix(ArrayList<String> l, Collection<String> prefixes)
	{
		return FL.toAL(l.stream().filter(s -> prefixes.stream().noneMatch(s::startsWith)));
	}
}