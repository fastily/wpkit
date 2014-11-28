package ft;

import java.util.ArrayList;
import java.util.Scanner;

import commons.CStrings;
import jwiki.core.CAction;
import jwiki.core.Wiki;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import util.FCLI;
import static ft.Core.*;
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
		CommandLine l = init(args, makeOptList(),
				"CCleaner [-dr|-t|[-p <title>|-u <user>|-c <cat> -f <filepath>] -r <reason>|-oos|-ur|-fu] [-d] [-a|-ac]");

		// Perform DR archiving if requested.
		if (l.hasOption('a'))
			DRArchive.main(new String[0]);
		else if (l.hasOption("ac"))
			processDRs();

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
				com.nukeLinksOnPage(l.getOptionValue('p'), rsn, "File");
			else if (l.hasOption('u'))
				com.nukeUploads(l.getOptionValue('u'), rsn);
			else if (l.hasOption('c'))
				com.categoryNuke(l.getOptionValue('c'), rsn, false);
			else if (l.hasOption('o'))
				com.clearOSD(rsn);
			else if (l.hasOption('f'))
				com.nukeFromFile(l.getOptionValue('f'), rsn);
		}
		else if (l.hasOption("dr")) // DR processing
			com.drDel(l.getOptionValue("dr"));
		else if (l.hasOption('t')) // Empty Talk Page clear from DBR
			talkPageClear();
		else
		// generic tasks. Should only run if 0 args specified, or something wasn't set right.
		{
			com.categoryNuke(CStrings.cv, CStrings.copyvio, false, "File");
			com.emptyCatDel(admin.getCategoryMembers(CStrings.osd, "Category"));
			com.emptyCatDel(admin.getCategoryMembers("Non-media deletion requests", "Category"));
			// com.nukeEmptyFiles(admin.getCategoryMembers(CStrings.osd, "File"));

			if (l.hasOption('d'))
				unknownClear();
		}
	}

	private static Options makeOptList()
	{
		Options ol = new Options();

		OptionGroup og = new OptionGroup();
		og.addOption(FCLI.makeArgOption("dr", "Delete all files linked in a DR", "DR"));
		og.addOption(FCLI.makeArgOption("p", "Set mode to delete all files linked on a page", "title"));
		og.addOption(FCLI.makeArgOption("u", "Set mode to delete all uploads by a user", "username"));
		og.addOption(FCLI.makeArgOption("c", "Set mode to delete all category members", "category"));
		og.addOption(FCLI.makeArgOption("f", "Set mode to delete all titles, separated by new line, in text file", "filepath"));
		og.addOption(new Option("o", false, "Delete all members of a Other Speedy Deletions"));
		og.addOption(new Option("t", false, "Clears orphaned talk pages from DBR"));
		og.addOption(new Option("a", false, "Archive DRs ready for archiving"));
		og.addOption(new Option("ac", false, "Close all Singleton DRs"));
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
	 * Deletes all pages on "Commons:Database reports/Orphaned talk pages".
	 * 
	 * @return A list of pages we failed to process
	 */
	private static ArrayList<String> talkPageClear()
	{
		ArrayList<String> l = new ArrayList<>();
		Scanner m = new Scanner(admin.getPageText("Commons:Database reports/Orphaned talk pages"));

		String ln;
		while (m.hasNextLine())
			if ((ln = m.nextLine()).contains("{{plnr"))
				l.add(ln.substring(ln.indexOf("=") + 1, ln.indexOf("}}")));
		m.close();

		return CAction.delete(admin, "Orphaned talk page", l);
	}

	/**
	 * Clears daily categories in Category:Unknown. List is grabbed from <a
	 * href="https://commons.wikimedia.org/wiki/User:FSV/UC">User:FSV/UC</a>
	 * 
	 * @return A list of pages we failed to process
	 */
	private static ArrayList<String> unknownClear()
	{
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
		return fails;

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
		ArrayList<Task> l = new ArrayList<Task>();
		String rsn = String.format("%s %s: %s", front, cat.substring(cat.indexOf("as of") + 6), back);

		for (String s : admin.getCategoryMembers(cat, "File"))
			l.add(new Task(s, null, rsn) {
				public boolean doJob(Wiki wiki)
				{
					return admin.delete(title, summary);
				}
			});
		return l;
	}

	/**
	 * Process (close & delete) all DRs on 'User:ArchiveBot/SingletonDR'
	 * 
	 * @return A list of titles we didn't process.
	 */
	private static ArrayList<String> processDRs()
	{
		ArrayList<ProcDR> dl = new ArrayList<>();
		for (String s : admin.getTemplatesOnPage("User:ArchiveBot/SingletonDR"))
			if (s.startsWith("Commons:Deletion requests/"))
				dl.add(new ProcDR(s));
		return Task.toString(admin.submit(dl, 20));
	}

	/**
	 * Represents a DR to process and close.
	 * 
	 * @author Fastily
	 * 
	 */
	private static class ProcDR extends Task
	{
		/**
		 * Constructor.
		 * 
		 * @param title The DR to process
		 */
		private ProcDR(String title)
		{
			super(title, null, String.format("[[%s]]", title));
		}

		/**
		 * Delete all files on the page and mark the DR as closed.
		 * 
		 * @param wiki The wiki object to use
		 * @return True if we were successful
		 */
		public boolean doJob(Wiki wiki)
		{
			for (String s : admin.getLinksOnPage(title, "File"))
				wiki.delete(s, summary);

			text = wiki.getPageText(title);
			return text != null ? wiki.edit(title, String.format("{{delh}}%n%s%n----%n'''Deleted''' -~~~~%n{{delf}}", text),
					"deleted") : false;
		}
	}
}