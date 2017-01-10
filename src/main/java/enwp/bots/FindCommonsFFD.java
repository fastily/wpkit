package enwp.bots;

import java.util.ArrayList;
import java.util.HashSet;

import ctools.util.TParse;
import ctools.util.Toolbox;
import ctools.util.WikiX;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;

/**
 * Finds files on enwp nominated for deletion on Commons and flags the local file.
 * 
 * @author Fastily
 *
 */
public class FindCommonsFFD
{
	/**
	 * The Wiki objects to use
	 */
	private static final Wiki enwp = Toolbox.getFastilyBot(), com = enwp.getWiki("commons.wikimedia.org");

	/**
	 * A Set of files nominated for deletion on Commons
	 */
	protected static final HashSet<String> ffdCom = initComFFD();

	/**
	 * Matches wikitext usages of Template:Now Commons
	 */
	private static final String ncRegex = TParse.makeTemplateRegex(enwp, "Template:Now Commons");

	/**
	 * The template String for Template:Nominated for deletion on Commons
	 */
	private static final String nfdc = "{{Nominated for deletion on Commons|%s}}";

	/**
	 * Main driver
	 * 
	 * @param args Program args, not used
	 */
	public static void main(String[] args)
	{
		procCat("Category:All Wikipedia files with the same name on Wikimedia Commons");
		procCat("Category:All Wikipedia files with a different name on Wikimedia Commons");
	}

	/**
	 * Inspect elements a specified category for files which are nominated for deletion on Commons
	 * 
	 * @param cat The Category to check.
	 */
	private static void procCat(String cat)
	{
		WikiX.getFirstOnlySharedDuplicate(enwp, enwp.getCategoryMembers(cat, NS.FILE)).entrySet().stream()
				.filter(e -> ffdCom.contains(e.getValue())).forEach(e -> enwp.replaceText(e.getKey(), ncRegex,
						String.format(nfdc, enwp.nss(e.getValue())), "BOT: Flag transfered file that is up for deletion on Commons"));
	}

	/**
	 * Compiles the Set of files nominated for deletion on Commons
	 * 
	 * @return The Set of files currently nominated for deletion on Commons
	 */
	private static HashSet<String> initComFFD()
	{
		ArrayList<String> cats = FL.toSAL("Category:Copyright violations", "Category:Other speedy deletions");
		cats.addAll(FL
				.toAL(FL.toSAL("Category:Media missing permission", "Category:Media without a license", "Category:Media without a source")
						.stream().flatMap(c -> com.getCategoryMembers(c, NS.CATEGORY).stream()).filter(s -> s.matches(".+?\\d{4}"))));

		HashSet<String> l = new HashSet<>(FL.toAL(cats.stream().flatMap(c -> com.getCategoryMembers(c, NS.FILE).stream())));
		l.addAll(com.whatTranscludesHere("Template:Delete"));

		return l;
	}
}