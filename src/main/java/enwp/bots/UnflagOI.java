package enwp.bots;

import ctools.util.Toolbox;
import enwp.WTP;
import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;

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
	private static final Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * A regex matching the Orphan image template
	 */
	private static final String oiRegex = WTP.orphan.getRegex(wiki);

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, unused.
	 */
	public static void main(String[] args)
	{
		MQuery.fileUsage(wiki, wiki.whatTranscludesHere(WTP.orphan.title, NS.FILE)).forEach((k, v) -> {
			if(!wiki.filterByNS(v, NS.MAIN).isEmpty())
				wiki.replaceText(k, oiRegex, "BOT: File contains inbound links");
		});
	}
}