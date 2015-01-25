package ft;

import java.util.ArrayList;

import commons.CStrings;
import commons.Commons;
import jwiki.core.CAction;
import jwiki.core.Wiki;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import util.FCLI;
import util.WikiGen;
import static jwiki.core.MBot.Task;

/**
 * Assistant with common deletion jobs on Commons. Caveat: files should be manually reviewed in a browser before using
 * this, as this tool only performs deletions, and nothing else.
 * 
 * @author Fastily
 * 
 */
public class CCleaner
{
	/**
	 * The reason parameter we'll be using to delete with, if applicable.
	 */
	private static String rsn = "";

	/**
	 * Main driver.
	 * 
	 * @param args Prog args
	 */
	public static void main(String[] args)
	{
		CommandLine l = FCLI.gnuParse(makeOptList(), args,
				"CCleaner [-dr|-t|[-p <title>|-u <user>|-c <cat> -f <filepath>] -r <reason>|-oos|-ur|-fu] [-d]");

		Wiki wiki = WikiGen.wg.get(1);
		
		// Set reason param if applicable.
		if (l.hasOption('r'))
			rsn = l.getOptionValue('r');
		else if (l.hasOption("oos"))
			rsn = CStrings.oos;
		else if (l.hasOption("ur"))
			rsn = CStrings.ur;
		else if (l.hasOption("house"))
			rsn = CStrings.house;
		else if (l.hasOption("fu"))
			rsn = CStrings.fu;

		// Check for special, reason-related deletion requests.
		if (!rsn.isEmpty())
		{
			if (l.hasOption('p'))
				Commons.nukeLinksOnPage(wiki, l.getOptionValue('p'), rsn, "File");
			else if (l.hasOption('u'))
				Commons.nukeUploads(wiki, l.getOptionValue('u'), rsn);
			else if (l.hasOption('c'))
				Commons.categoryNuke(wiki, l.getOptionValue('c'), rsn, false);
			else if (l.hasOption('o'))
				Commons.categoryNuke(wiki, CStrings.osd, rsn, false);
			else if (l.hasOption('f'))
				Commons.nukeFromFile(wiki, l.getOptionValue('f'), rsn);
		}
		else if (l.hasOption("dr")) // DR processing
			Commons.drDel(wiki, l.getOptionValue("dr"));
		else
		// generic tasks. Should only run if 0 args specified, or something wasn't set right.
		{
			Commons.categoryNuke(wiki, CStrings.cv, CStrings.copyvio, false, "File");
			Commons.emptyCatDel(wiki, wiki.getCategoryMembers(CStrings.osd, "Category"));
			Commons.emptyCatDel(wiki, wiki.getCategoryMembers("Non-media deletion requests", "Category"));
			Commons.nukeEmptyFiles(wiki, wiki.getCategoryMembers(CStrings.osd, "File"));

			if (l.hasOption('d'))
				unknownClear();
		}
	}

	private static Options makeOptList()
	{
		Options ol = FCLI.makeDefaultOptions();

		OptionGroup og = new OptionGroup();
		og.addOption(FCLI.makeArgOption("dr", "Delete all files linked in a DR", "DR"));
		og.addOption(FCLI.makeArgOption("p", "Set mode to delete all files linked on a page", "title"));
		og.addOption(FCLI.makeArgOption("u", "Set mode to delete all uploads by a user", "username"));
		og.addOption(FCLI.makeArgOption("c", "Set mode to delete all category members", "category"));
		og.addOption(FCLI.makeArgOption("f", "Set mode to delete all titles, separated by new line, in text file", "filepath"));
		og.addOption(new Option("o", false, "Delete all members of a Other Speedy Deletions"));
		og.addOption(new Option("t", false, "Clears orphaned talk pages from DBR"));
		ol.addOptionGroup(og);

		og = new OptionGroup();
		og.addOption(FCLI.makeArgOption("r", "Reason param, for use with options that require a reason", "reason"));
		og.addOption(new Option("oos", false, "Auto sets reason param to 'out of project scope'"));
		og.addOption(new Option("ur", false, "Auto sets reason param to 'user requested for own upload'"));
		og.addOption(new Option("house", false, "Auto sets reason param to 'housekeeping'"));
		og.addOption(new Option("fu", false, "Auto sets reason param to 'fair use is disallowed'"));
		ol.addOptionGroup(og);

		ol.addOption("d", false, "Deletes everything we can in Category:Unknown");

		return ol;
	}

	/**
	 * Clears daily categories in Category:Unknown. List is grabbed from <a
	 * href="https://commons.wikimedia.org/wiki/User:FSV/UC">User:FSV/UC</a>
	 * 
	 * @return A list of pages we failed to process
	 */
	private static ArrayList<String> unknownClear()
	{
		/*
		user.nullEdit("User:FastilyClone/UC");

		ArrayList<Task> l = new ArrayList<>();
		String baseLS = "you may [[Special:Upload|re-upload]] the file, but please %s";

		ArrayList<String> cats = admin.getLinksOnPage(true, "User:FastilyClone/UC");
		for (String c : cats)
		{
			if (c.contains("permission"))
				l.addAll(genUCDI(c, "[[COM:OTRS|No permission]] since", CStrings.baseP));
			else if (c.contains("license"))
				l.addAll(genUCDI(c, "No license since", String.format(baseLS, "include a [[COM:CT|license tag]]")));
			else
				l.addAll(genUCDI(c, "No source since", String.format(baseLS, "cite the file's source")));
		}

		ArrayList<String> fails = Task.toString(admin.submit(l, 20));
		com.emptyCatDel(cats);
		return fails;*/
		
		return null;

	}

	/**
	 * Helper for unknownClear(). Parse out date from category and generate reason params for items to delete
	 * 
	 * @param cat The category to process
	 * @param front The front part of the reason, before the colon.
	 * @param back The back part of the reason, after the colon.
	 * @return A list of DeleteItems we created.
	 */
	private static ArrayList<Task> genUCDI(String cat, String front, String back)
	{
		/*
		ArrayList<Task> l = new ArrayList<Task>();
		String rsn = String.format("%s %s: %s", front, cat.substring(cat.indexOf("as of") + 6), back);

		for (String s : admin.getCategoryMembers(cat, "File"))
			l.add(new Task(s, null, rsn) {
				public boolean doJob(Wiki wiki)
				{
					return admin.delete(title, summary);
				}
			});
		return l;*/
		
		return null;
	}
}