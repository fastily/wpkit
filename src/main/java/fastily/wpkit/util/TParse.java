package fastily.wpkit.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;

/**
 * Static, MediaWiki Template parsing methods.
 * 
 * @author Fastily
 *
 */
public class TParse
{
	/**
	 * A capturing group that matches any reserved regex operator character in the Java Pattern API.
	 */
	private static final String rrc = String.format("([%s])", Pattern.quote("()[]{}<>\\^-=$!|?*+."));

	/**
	 * Constructors disallowed
	 */
	private TParse()
	{

	}

	/**
	 * Escapes reserved regex characters of the Java Pattern API in a String.
	 * 
	 * @param s The String to escape regex chars from
	 * @return A copy of <code>s</code> with reserved regex chars escaped.
	 */
	private static String escapeRegexChars(String s)
	{
		return s.replaceAll(rrc, "\\\\" + "$1");
	}

	/**
	 * Constructs a regular expression which will match the specified template and its parameters.
	 * 
	 * @param wiki The wiki object to use
	 * @param tplate The template (including namespace) to generate a regex for.
	 * @return A regex matching the specified template, its redirects, and parameters.
	 */
	public static String makeTemplateRegex(Wiki wiki, String tplate)
	{
		ArrayList<String> l = wiki.whatLinksHere(tplate, true);
		l.add(wiki.nss(tplate));

		return TParse.makeTitleRegex(WikiX.stripNamespaces(wiki, l));
	}

	/**
	 * Makes a regex which will match templates of the specified titles. The entire template will be matched, including
	 * internal whitespace and nested parameters. WARNING: Method does not strip namespaces.
	 * 
	 * @param titles The titles of templates to match
	 * @return The regex
	 */
	public static String makeTitleRegex(ArrayList<String> titles)
	{
		ArrayList<String> l = new ArrayList<>();
		for (String s : titles)
			l.add(escapeRegexChars(s).replaceAll("( |_)", "( |_)"));

		return String.format("(?si)\\{\\{\\s*?(%s)\\s*?(\\||\\{\\{.+?\\}\\}|.+?)*?\\}\\}", FL.pipeFence(l));
	}

	/**
	 * Extracts a template from text.
	 * 
	 * @param p The template's Regex
	 * @param text The text to look for <code>p</code> in
	 * @return The template, or the empty string if nothing was found.
	 */
	public static String extractTemplate(Pattern p, String text)
	{
		Matcher m = p.matcher(text);
		return m.find() ? m.group() : "";
	}
}