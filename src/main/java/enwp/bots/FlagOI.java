package enwp.bots;

import java.util.ArrayList;
import java.util.HashSet;

import ctools.util.Toolbox;
import enwp.WTP;
import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;

/**
 * Finds and flags orphaned free media files on enwp.
 * 
 * @author Fastily
 *
 */
public class FlagOI
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		HashSet<String> l = Toolbox.fetchLabsReportListAsFiles(wiki, "report3");
		l.removeAll(WTP.orphan.getTransclusionSet(wiki, NS.FILE));
		l.removeAll(WTP.nobots.getTransclusionSet(wiki, NS.FILE));
		l.removeAll(Toolbox.fetchLabsReportListAsFiles(wiki, "report4"));
		l.removeAll(new HashSet<>(MQuery.exists(wiki, false, new ArrayList<>(l))));
		
		l.forEach(s -> wiki.addText(s, "\n{{Orphan image}}", "BOT: Noting that file has no inbound file usage", false));
	}
}