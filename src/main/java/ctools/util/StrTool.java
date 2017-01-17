package ctools.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	 * Gets the first substring of a String matching a regex.
	 * 
	 * @param p The regex to match
	 * @param s The String to find the regex-matching substring in
	 * @return The substring, or the empty string if no matches were found.
	 */
	public static String substringFromRegex(Pattern p, String s)
	{
		Matcher m = p.matcher(s);
		return m.find() ? m.group() : "";
	}

	/**
	 * Inserts a String into a String. PRECONDITION: <code>index</code> is a valid index for <code>s</code>
	 * 
	 * @param s The string to insert into
	 * @param insert The String to be inserted
	 * @param index The index to insert <code>insert</code> at. The original character at this index will be shifted down
	 *           one slot to make room for <code>insert</code>
	 * @return The modified String.
	 */
	public static String insertAt(String s, String insert, int index)
	{
		return new StringBuffer(s).insert(index, insert).toString();
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