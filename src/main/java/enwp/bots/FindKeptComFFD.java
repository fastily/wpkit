package enwp.bots;

import java.util.HashSet;

import ctools.util.Toolbox;
import ctools.util.WikiX;
import enwp.WTP;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;

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
	private static final String nfdcRegex = WTP.nomDelOnCom.getRegex(wiki);

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		HashSet<String> cffdl = FindCommonsFFD.findComFFD();

		WikiX.getFirstOnlySharedDuplicate(wiki,
				wiki.getCategoryMembers("Category:Files nominated for deletion on Wikimedia Commons", NS.FILE)).forEach((k, v) -> {
					if (!cffdl.contains(wiki.convertIfNotInNS(v, NS.FILE)))
						wiki.replaceText(k, nfdcRegex, String.format(ManageMTC.ncd, v), "BOT: File is not up for deletion on Commons");
				});
	}
}