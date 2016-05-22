package enwp.reports;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
	 * The title of the report page
	 */
	private static final String reportPage = "User:FastilyBot/Recently Untagged Dated Deletion Files";

	/**
	 * The list of root categories to inspect
	 */
	private static final ArrayList<String> ddCat = FL.toSAL("Category:Wikipedia files with unknown source",
			"Category:Wikipedia files with unknown copyright status", "Category:Wikipedia files missing permission",
			"Category:Disputed non-free Wikipedia files", "Category:Replaceable non-free use Wikipedia files");

	/**
	 * The regex matching eligible daily deletion categories for review
	 */
	private static final String ddCatRegex = ".*? " + WPStrings.DMYRegex;

	/**
	 * The local storage path for caching the previous run's daily deletion files
	 */
	private static final Path wpddfiles = Paths.get("WPDDFiles.txt");

	/**
	 * The maximum number of old reports to keep on the <code>reportPage</code>.
	 */
	private static final int maxOldReports = 13;
	
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
		
		ArrayList<String> sections = sectionSplit(wiki.getPageText(reportPage));
		if(sections.size() > maxOldReports)
			sections = new ArrayList<>(sections.subList(0, maxOldReports));
			
		wiki.edit(reportPage,
				WTool.listify("\n== ~~~~~ ==\n", MQuery.exists(wiki, true, new ArrayList<>(cacheList)), true)
						+ sections.stream().collect(Collectors.joining()),
				"Updating report");

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

	/**
	 * A dumb page section splitting method. PRECONDITION: The text being parsed contains level 2 headers
	 * 
	 * @param text The text to split into sections
	 * @return An ArrayList with the sections of the page.
	 */
	private static ArrayList<String> sectionSplit(String text)
	{
		ArrayList<Integer> indexList = new ArrayList<>();
		Matcher m = Pattern.compile("(?m)^\\=\\=").matcher(text);
		while (m.find())
			indexList.add(m.start());

		if (indexList.size() <= 1)
			return FL.toSAL(text);

		ArrayList<String> l = new ArrayList<>();
		for (int i = 0; i < indexList.size() - 1; i++)
			l.add(text.substring(indexList.get(i), indexList.get(i + 1)));

		l.add(text.substring(indexList.get(indexList.size() - 1)));

		return l;
	}
}