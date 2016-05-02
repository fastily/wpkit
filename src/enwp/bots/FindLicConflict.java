package enwp.bots;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;

import jwiki.core.NS;
import jwiki.core.Wiki;
import jwikix.util.FSystem;
import jwikix.util.WikiGen;

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
	private static final Wiki wiki = WikiGen.wg.get("FastilyBot", "en.wikipedia.org");

	/**
	 * The configurable list of templates to skip.
	 */
	private static final ArrayList<String> ignoreList = wiki.getLinksOnPage("User:FastilyBot/Task5Ignore");

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args) throws Throwable
	{
		HashSet<String> fl = read("Category:All free media"), nfl = read("Category:All non-free media");

		for (String s : ignoreList)
		{
			ArrayList<String> l = wiki.whatTranscludesHere(s);
			nfl.removeAll(l);
			fl.removeAll(l);
		}

		fl.retainAll(nfl);

		int i = 0;
		for (String s : fl)
		{
			if (i++ > 50)
				break;

			wiki.addText(s, "{{Wrong-license}}\n", "BOT: Tag file with copyright status conflict", true);
		}
	}

	/**
	 * Caches the list of free and non-free files. This is temporary and will be removed in future versions of this bot.
	 * 
	 * @param cat The category to retrieve files for.
	 * @return The list of files in the category.
	 * @throws Throwable IO error.
	 */
	private static HashSet<String> read(String cat) throws Throwable
	{

		Path fn = Paths.get(FSystem.home, "Desktop", wiki.nss(cat));
		if (!Files.exists(fn))
			Files.write(fn, wiki.getCategoryMembers(cat, NS.FILE), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

		return new HashSet<>(Files.readAllLines(fn));
	}
}