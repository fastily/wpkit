package enwp.reports;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import jwiki.core.MQuery;
import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwikix.core.WikiGen;
import util.Toolbox;
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
		HashSet<String> spiCases = FL.toSet(wiki.prefixIndex(NS.PROJECT, "Sockpuppet investigations/").stream()
				.filter(s -> !(s.endsWith("/Archive") || s.startsWith("Wikipedia:Sockpuppet investigations/SPI/"))));

		spiCases.removeAll(wiki.whatTranscludesHere("Template:SPI case status", NS.PROJECT));
		spiCases.removeAll(wiki.whatTranscludesHere("Template:SPI archive notice", NS.PROJECT));
		spiCases.removeAll(wiki.getLinksOnPage(report + "/Ignore"));

		wiki.edit(report,
				Toolbox.listify("{{/Header}}\n" + WPStrings.updatedAt, MQuery.resolveRedirects(wiki, new ArrayList<>(spiCases))
						.entrySet().stream().filter(e -> e.getKey().equals(e.getValue())).map(Map.Entry::getValue), false),
				"BOT: Update list");
	}
}