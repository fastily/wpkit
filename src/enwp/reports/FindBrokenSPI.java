package enwp.reports;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import jwiki.core.MQuery;
import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwikix.util.WTool;
import jwikix.util.WikiGen;
import util.WPStrings;

/**
 * Finds broken SPI pages on enwp.
 * 
 * @author Fastily
 *
 */
public final class FindBrokenSPI
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = WikiGen.wg.get("FastilyBot", "en.wikipedia.org");

	/**
	 * The set of non-malformed SPI pages.
	 */
	private static final HashSet<String> properPages = fetchProperPages();

	/**
	 * The title to post reports on
	 */
	private static final String report = "Wikipedia:Sockpuppet investigations/SPI/Malformed Cases Report";

	/**
	 * Main driver
	 * 
	 * @param args No args, not used.
	 */
	public static void main(String[] args)
	{
		ArrayList<String> l = FL.toAL(MQuery
				.resolveRedirects(wiki,
						FL.toAL(wiki.prefixIndex(NS.PROJECT, "Sockpuppet investigations/").stream()
								.filter(s -> !(s.endsWith("/Archive") || s.startsWith("Wikipedia:Sockpuppet investigations/SPI/")
										|| properPages.contains(s)))))
				.entrySet().stream().filter(e -> e.getKey().equals(e.getValue())).map(Map.Entry::getValue));

		wiki.edit(report, WTool.listify("{{/Header}}\n" + WPStrings.updatedAt, l, false), "Update list");
	}

	/**
	 * Fetches pages which have the appropriate templates and ignore list pages.
	 * 
	 * @return The Set of non-malformed SPI pages.
	 */
	private static HashSet<String> fetchProperPages()
	{
		HashSet<String> l = new HashSet<>();
		for (String s : new String[] { "Template:SPI archive notice", "Template:SPI case status" })
			l.addAll(wiki.whatTranscludesHere(s, NS.PROJECT));

		l.addAll(wiki.getLinksOnPage(report + "/Ignore"));

		return l;
	}
}