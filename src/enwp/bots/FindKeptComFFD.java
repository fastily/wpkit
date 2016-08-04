package enwp.bots;

import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwikix.core.MQueryX;
import jwikix.core.TParse;
import util.Toolbox;

/**
 * Finds local enwp files which were nominated for deletion on Commons but kept.
 * 
 * @author Fastily
 *
 */
public final class FindKeptComFFD
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = Toolbox.getFastilyBot();

	/**
	 * The regex matching Template:Nominated for deletion on Commons.
	 */
	private static final String nfdcRegex = TParse.makeTemplateRegex(wiki, "Template:Nominated for deletion on Commons");

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		FL.mapToList(MQueryX.getFirstOnlySharedDuplicate(wiki,
				wiki.getCategoryMembers("Category:Files nominated for deletion on Wikimedia Commons", NS.FILE))).stream()
				.filter(t -> !FindCommonsFFD.ffdCom.contains(t.y)).forEach(t -> wiki.replaceText(t.x, nfdcRegex,
						String.format(ManageMTC.ncd, t.y), "BOT: File is no longer nominated for deletion on Commons"));
	}
}