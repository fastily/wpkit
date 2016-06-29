package enwp.bots;

import java.util.Map;

import jwiki.core.MQuery;
import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.GroupQueue;
import jwikix.core.TParse;
import jwikix.core.WikiGen;

/**
 * Reomves {{Orphan image}} from freely licensed files which contain file links in the main space.
 * 
 * @author Fastily
 *
 */
public final class UnflagOI
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = WikiGen.wg.get("FastilyBot", "en.wikipedia.org");

	/**
	 * The full title for the Orphan Image template
	 */
	private static final String oiTempl = "Template:Orphan image";

	/**
	 * A regex matching the Orphan image template
	 */
	private static final String oiRegex = TParse.makeTemplateRegex(wiki, oiTempl);

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, unused.
	 */
	public static void main(String[] args)
	{
		GroupQueue<String> gq = new GroupQueue<>(wiki.whatTranscludesHere(oiTempl), 50);
		while (gq.has())
			for (String s : FL.toAL(MQuery.fileUsage(wiki, gq.poll()).entrySet().stream()
					.filter(e -> !wiki.filterByNS(e.getValue(), NS.MAIN).isEmpty()).map(Map.Entry::getKey)))
				wiki.replaceText(s, oiRegex, "BOT: Removing {{Orphan Image}} from a non-orphaned file");

	}
}