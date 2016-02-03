package enwp;

import java.util.ArrayList;

import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.extras.WikiGen;
import jwiki.util.FL;
import util.WTool;

/**
 * Finds broken SPI pages on enwp.
 * 
 * @author Fastily
 *
 */
public class FindBrokenSPI
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
	 * A list of pages to omit from the report
	 */
	private static final ArrayList<String> ignoreList = wiki
			.getLinksOnPage("Wikipedia:Sockpuppet investigations/SPI/Malformed Cases Report/Ignore");

	/**
	 * Main driver
	 * 
	 * @param args No args, not used.
	 */
	public static void main(String[] args)
	{
		ArrayList<String> l = FL.toAL(wiki.prefixIndex(NS.PROJECT, "Sockpuppet investigations/").parallelStream()
				.filter(s -> !s.endsWith("/Archive") && !s.startsWith("Wikipedia:Sockpuppet investigations/SPI/"))
				.filter(s -> !archived.contains(s) && !inProg.contains(s) && !ignoreList.contains(s))
				.filter(s -> wiki.resolveRedirect(s).equals(s)));

		wiki.edit("Wikipedia:Sockpuppet investigations/SPI/Malformed Cases Report",
				WTool.listify("{{SPI navigation}}\n" + WPStrings.updatedAt, l, false), "Update list");
	}
}