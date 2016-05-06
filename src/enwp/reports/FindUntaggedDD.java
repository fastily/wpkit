package enwp.reports;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import jwiki.core.MQuery;
import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwikix.util.WTool;
import jwikix.util.WikiGen;
import util.WPStrings;

import static java.nio.file.StandardOpenOption.*;

/**
 * Finds and reports on files in daily deletion categories which have recently been untagged.
 * 
 * @author Fastily
 *
 */
public final class FindUntaggedDD
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = WikiGen.wg.get("FastilyBot", "en.wikipedia.org");

	/**
	 * The list of root categories to inspect
	 */
	private static final ArrayList<String> ddCat = FL.toSAL("Category:Wikipedia files with unknown source",
			"Category:Wikipedia files with unknown copyright status", "Category:Wikipedia files missing permission",
			"Category:Wikipedia files with no non-free use rationale", "Category:Disputed non-free Wikipedia files",
			"Category:Replaceable non-free use Wikipedia files");

	/**
	 * The regex matching eligible daily deletion categories for review
	 */
	private static final String ddCatRegex = ".*? " + WPStrings.DMYRegex;

	/**
	 * The local storage path for caching the previous run's daily deletion files
	 */
	private static final Path wpddfiles = Paths.get("WPDDFiles.txt");

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used
	 * @throws Throwable On IO error
	 */
	public static void main(String[] args) throws Throwable
	{
		HashSet<String> l = FL.toSet(ddCat.stream().flatMap(rootCat -> wiki.getCategoryMembers(rootCat, NS.CATEGORY).stream())
				.filter(cat -> cat.matches(ddCatRegex)).flatMap(cat -> wiki.getCategoryMembers(cat, NS.FILE).stream()));

		if (!Files.exists(wpddfiles))
			dump(l, true);

		HashSet<String> cacheList = FL.toSet(Files.lines(wpddfiles));
		cacheList.removeAll(l);

		if (!cacheList.isEmpty())
			wiki.addText("User:FastilyBot/UntaggedDDFiles",
					WTool.listify("\n== ~~~~~ ==\n", MQuery.exists(wiki, true, new ArrayList<>(cacheList)), true), "Updating report", true);

		dump(l, false);
	}

	/**
	 * Dumps a HashSet to <code>wpddfiles</code>
	 * 
	 * @param l The HashSet to use
	 * @param exit Set true to exit the program after this method completes.
	 * @throws Throwable IO Error.
	 */
	private static void dump(HashSet<String> l, boolean exit) throws Throwable
	{
		Files.write(wpddfiles, l, CREATE, WRITE, TRUNCATE_EXISTING);
		if (exit)
			System.exit(0);
	}
}