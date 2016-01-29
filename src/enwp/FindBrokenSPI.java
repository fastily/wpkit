package enwp;

import java.util.ArrayList;

import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.extras.WikiGen;
import jwiki.util.FL;

/**
 * Finds broken SPI pages
 * 
 * @author Fastily
 *
 */
public class FindBrokenSPI
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = WikiGen.wg.get("FastilyClone", "en.wikipedia.org");

	/**
	 * The list of archived SPI cases
	 */
	private static final ArrayList<String> archived = wiki.whatTranscludesHere("Template:SPI archive notice");

	/**
	 * The list of in-progress SPI cases
	 */
	private static final ArrayList<String> inProg = wiki.whatTranscludesHere("Template:SPI case status");

	/**
	 * Main driver
	 * 
	 * @param args No args, not used.
	 */
	public static void main(String[] args)
	{
		dump(FL.toAL(wiki.prefixIndex(NS.PROJECT, "Sockpuppet investigations/").parallelStream()
				.filter(s -> !s.endsWith("/Archive") && !s.startsWith("Wikipedia:Sockpuppet investigations/SPI/"))
				.filter(s -> !archived.contains(s) && !inProg.contains(s)).filter(s -> wiki.resolveRedirect(s).equals(s))));
	}

	/**
	 * Dumps a list of potentially problematic pages to my Sandbox
	 * 
	 * @param l The list of titles to dump and link
	 */
	private static void dump(ArrayList<String> l)
	{
		String x = "";
		for (String s : l)
			x += String.format("* [[%s]]%n", s);

		wiki.edit("User:Fastily/Sandbox1", x, "Update list");
	}
}