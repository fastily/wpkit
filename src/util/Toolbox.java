package util;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jwiki.core.Wiki;

/**
 * Miscellaneous custom functions common to my scripts/bots.
 * 
 * @author Fastily
 *
 */
public final class Toolbox
{
	/**
	 * Constructors disallowed
	 */
	private Toolbox()
	{

	}

	/**
	 * Parses a config page with key-value pairs. Empty lines and lines starting with '&gt;' are ignored. Key-value pairs
	 * should be split by <code>;</code>, one pair per line.
	 * 
	 * @param wiki The Wiki object to use
	 * @param title The title of the config page to parse
	 * @return A HashMap with the parsed pairs.
	 */
	public static HashMap<String, String> fetchConfig(Wiki wiki, String title)
	{
		return new HashMap<>(Stream.of(wiki.getPageText(title).split("\n")).filter(s -> !s.startsWith("<") && !s.isEmpty())
				.map(s -> s.split(";", 2)).collect(Collectors.toMap(a -> a[0], a -> a[1])));
	}
}