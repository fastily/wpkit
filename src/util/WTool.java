package util;

import java.util.ArrayList;

import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.FString;

/**
 * Miscellaneous Wiki-related routines.
 * 
 * @author Fastily
 *
 */
public class WTool
{
	/**
	 * Constructors disallowed
	 */
	private WTool()
	{

	}

	/**
	 * Generates a Wiki-text ready, wiki-linked, unordered list from a list of titles.
	 * 
	 * @param header A header/lead string to apply at the beginning of the returned String.
	 * @param titles The titles to use
	 * @param doEscape Set as true to escape titles. i.e. adds a <code>:</code> before each link so that files and
	 *           categories are properly escaped and appear as links.
	 * @return A String with the titles as a linked, unordered list, in Wiki-text.
	 */
	public static String listify(String header, ArrayList<String> titles, boolean doEscape)
	{
		String fmtStr = "* [[" + (doEscape ? ":" : "") + "%s]]\n";

		String x = "" + header;
		for (String s : titles)
			x += String.format(fmtStr, s);

		return x;
	}
	
	/**
	 * Constructs a regular expression which will match the specified template and its parameters.
	 * 
	 * @param wiki The wiki object to use
	 * @param tplate The template (including namespace) to generate a regex for.
	 * @return A regex matching the specified template, its redirects, and parameters.
	 */
	public static String makeTRegex(Wiki wiki, String tplate)
	{
		ArrayList<String> l = wiki.whatLinksHere(tplate, true);
		l.add(wiki.nss(tplate));

		return makeTemplateRegex(stripNamespaces(wiki, l));
	}

	/**
	 * Makes a regex which will match templates of the specified titles. The entire template will be matched, including
	 * internal whitespace and nested parameters. WARNING: Method does not strip namespaces.
	 * 
	 * @param titles The titles of templates to match
	 * @return The regex
	 */
	public static String makeTemplateRegex(ArrayList<String> titles)
	{
		ArrayList<String> l = new ArrayList<>();
		for (String s : titles)
			l.add(StrTool.escapeRegexChars(s).replaceAll("( |_)", "( |_)"));

		return String.format("(?si)\\{\\{\\s*?(%s)\\s*?((\\p{Alnum}*?\\s*?\\=)??(\\s|\\||\\p{Alnum}|\\{\\{.+?\\}\\}))*?\\}\\}",
				FString.fenceMaker("|", l));
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
}