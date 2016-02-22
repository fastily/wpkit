package enwp;

import java.util.ArrayList;
import java.util.Map;

import jwiki.core.MQuery;
import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.WikiGen;
import util.WTool;

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
	 * The list of archived SPI cases
	 */
	private static final ArrayList<String> archived = wiki.whatTranscludesHere("Template:SPI archive notice");

	/**
	 * The list of in-progress SPI cases
	 */
	private static final ArrayList<String> inProg = wiki.whatTranscludesHere("Template:SPI case status");

	/**
	 * The title to post reports on
	 */
	private static final String report = "Wikipedia:Sockpuppet investigations/SPI/Malformed Cases Report";

	/**
	 * A list of pages to omit from the report
	 */
	private static final ArrayList<String> ignoreList = wiki.getLinksOnPage(report + "/Ignore");

	/**
	 * Main driver
	 * 
	 * @param args No args, not used.
	 */
	public static void main(String[] args)
	{
		ArrayList<String> l = FL.toAL(MQuery
				.resolveRedirects(wiki,
						FL.toAL(wiki.prefixIndex(NS.PROJECT, "Sockpuppet investigations/").parallelStream()
								.filter(s -> !s.endsWith("/Archive") && !s.startsWith("Wikipedia:Sockpuppet investigations/SPI/"))
								.filter(s -> !archived.contains(s) && !inProg.contains(s) && !ignoreList.contains(s))))
				.entrySet().stream().filter(e -> e.getKey().equals(e.getValue())).map(Map.Entry::getValue));

		wiki.edit(report, WTool.listify("{{/Header}}\n" + WPStrings.updatedAt, l, false), "Update list");
	}
}