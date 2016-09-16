package enwp.bots;

import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.Toolbox;

/**
 * Finds enwp files which are flagged as both free and non-free.
 * 
 * @author Fastily
 *
 */
public final class FindLicConflict
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
	public static void main(String[] args) throws Throwable
	{
		HashSet<String> fl = new HashSet<>(wiki.getCategoryMembers("Category:All free media", NS.FILE));
		wiki.getLinksOnPage("User:FastilyBot/Task5Ignore").stream().forEach(s -> fl.removeAll(wiki.whatTranscludesHere(s, NS.FILE)));

		fl.retainAll(new HashSet<>(wiki.getCategoryMembers("Category:All non-free media", NS.FILE)));

		fl.stream().forEach(s -> wiki.addText(s, "{{Wrong-license}}\n", "BOT: Noting possible conflict in copyright status", true));
	}
}