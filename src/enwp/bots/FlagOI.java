package enwp.bots;

import java.util.ArrayList;
import java.util.HashSet;

import jwiki.core.MQuery;
import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.GroupQueue;
import jwikix.core.WikiGen;
import jwikix.util.StrTool;
import util.Toolbox;

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
	private static final Wiki wiki = WikiGen.wg.get("FastilyBot", "en.wikipedia.org");

	/**
	 * Files linked to pages with the specified title prefixes will be ignored. In other words, the are not counted as a
	 * file use
	 */
	private static final HashSet<String> ignorePrefixes = Toolbox.fetchSimpleConfig(wiki, "User:FastilyBot/Task10Ignore");

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		HashSet<String> l = new HashSet<>(wiki.getCategoryMembers("Category:All free media", NS.FILE));
		l.removeAll(new HashSet<>(wiki.getCategoryMembers("Category:Wikipedia orphaned files", NS.FILE)));

		GroupQueue<String> gq = new GroupQueue<>(new ArrayList<>(l), 50);
		while (gq.has())
			FL.mapToList(MQuery.fileUsage(wiki, gq.poll())).stream().filter(t -> StrTool.omitStrWithPrefix(t.y, ignorePrefixes).isEmpty())
					.forEach(t -> wiki.addText(t.x, "\n{{Orphan image}}", "BOT: Noting that file has no inbound file usage", false));
	}
}