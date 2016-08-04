package enwp.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jwiki.core.MQuery;
import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwikix.util.StrTool;
import jwikix.util.Triple;
import util.Toolbox;
import util.WPStrings;

/**
 * Looks for pages nominated for XfD with no corresponding XfD page/entry.
 * 
 * @author Fastily
 *
 */
public class FindOrphanedXfD
{
	/**
	 * The list of pages to check. Order for each triple is ( template, entry-prefix, namespace).
	 */
	private static final List<Triple<String, ArrayList<String>, NS>> l = Arrays.asList(
			new Triple<>("Template:Ffd", FL.toSAL("Wikipedia:Files for discussion"), NS.FILE),
			new Triple<>("Template:Article for deletion", FL.toSAL("Wikipedia:Articles for deletion"), NS.MAIN),
			new Triple<>("Template:Template for discussion", FL.toSAL("Wikipedia:Templates for discussion"), NS.TEMPLATE));

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
		wiki.edit("User:FastilyBot/Orphaned XfD",
				Toolbox.listify(WPStrings.updatedAt,
						l.stream()
								.flatMap(t -> MQuery.linksHere(wiki, false, wiki.whatTranscludesHere(t.x, t.z)).entrySet().stream()
										.filter(e -> !StrTool.hasStrWithPrefix(e.getValue(), t.y)).map(Map.Entry::getKey)),
						true),
				"Updating report");
	}
}