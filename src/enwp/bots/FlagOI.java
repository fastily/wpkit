package enwp.bots;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.GroupQueue;
import fastily.jwikix.util.StrTool;
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
	private static final Wiki wiki = Toolbox.getFastilyBot();

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
		l.removeAll(wiki.allPages(null, false, true, -1, NS.FILE)); // avoid these because they often have many usages
		l.removeAll(wiki.whatTranscludesHere("Template:Bots", NS.FILE));
		
		GroupQueue<String> gq = new GroupQueue<>(new ArrayList<>(l), 50);
		while (gq.has())
			FL.mapToList(MQuery.fileUsage(wiki, gq.poll())).stream().filter(t -> StrTool.omitStrWithPrefix(t.y, ignorePrefixes).isEmpty())
					.forEach(t -> wiki.addText(t.x, "\n{{Orphan image}}", "BOT: Noting that file has no inbound file usage", false));
	}
}