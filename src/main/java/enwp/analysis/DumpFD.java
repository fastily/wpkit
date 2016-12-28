package enwp.analysis;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import enwp.WTP;
import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.Tuple;
import mtc.MTC;
import ctools.util.FCLI;
import ctools.util.Toolbox;

/**
 * Generates a report where each matching file found is dumped out username + <code>/MTCSources</code>.
 * 
 * @author Fastily
 *
 */
public final class DumpFD
{
	/**
	 * The source to fetch files from. This can be a category, template, or username.
	 */
	private static String srcMTC = "Category:Copy to Wikimedia Commons reviewed by a human";

	/**
	 * The maximum number of items to generate a report for.  Set -1 to disable.
	 */
	private static int max = 150;

	/**
	 * Regexes matching MTC and Orphan image templates.
	 */
	private static String mtcRegex, orRegex;
	
	/**
	 * Main driver
	 * 
	 * @param args Program args
	 */
	public static void main(String[] args) throws Throwable
	{
		CommandLine cl = FCLI.gnuParse(makeOpts(), args, "DumpFD [-m] [-s]");
		Wiki wiki = Toolbox.getFastilyClone();
		mtcRegex = WTP.mtc.getRegex(wiki);
		orRegex = WTP.orphan.getRegex(wiki);
		
		if (cl.hasOption('m'))
			max = Integer.parseInt(cl.getOptionValue('m'));
		if (cl.hasOption('s'))
			srcMTC = cl.getOptionValue('s');

		ArrayList<String> l = null;

		switch (wiki.whichNS(srcMTC).v)
		{
			case 0: // user, but sometimes w/o namespace.
			case 2: // user
				l = wiki.getUserUploads(srcMTC);
				break;
//			case 10: // template transclusions
//				l = prepWS(wiki.whatTranscludesHere(srcMTC, NS.FILE));
//				break;
			case 14: // category members
				l = wiki.getCategoryMembers(srcMTC, max, NS.FILE);
				break;
			default:
				throw new IllegalArgumentException(srcMTC + " is not a valid source for MTC files");
		}
		
		String x = "Report Generated @ ~~~~~\n{| class=\"wikitable sortable\" style=\"margin-left: auto; margin-right: auto;\"\n! Title \n! File \n! Desc\n|-\n";

		ArrayList<String> tl = new MTC(wiki).canTransfer(l);
		for (Tuple<String, String> e : FL.mapToList(MQuery.getPageText(wiki, tl)))
			x += String.format("| [[:%s]]%n%s%n| [[%s|center|200px]]%n| %s%n|-%n", e.x, wiki.getRevisions(e.x, 1, true, null, null).get(0).user, e.x, stripText(e.y));

		x += "|}\n";

		wiki.edit(String.format("User:%s/MTCSources", wiki.whoami()), x, "Updating report");
		wiki.edit(String.format("User:%s/MTCSources/List", wiki.whoami()), Toolbox.listify("", tl, true), "Updating report");
	}

	/**
	 * Strips commonly used Templates from output.
	 * @param text The text to process
	 * @return A cleaner version of the text.
	 */
	private static String stripText(String text)
	{		
		return text.replaceAll("(?m)^\\=\\=.+?\\=\\=$\\s*", "").replaceAll(mtcRegex, "").replaceAll(orRegex, "");
	}

	/**
	 * Creates CLI options for DumpFD
	 * 
	 * @return CLI Options for the application.
	 */
	private static Options makeOpts()
	{
		Options ol = FCLI.makeDefaultOptions();
		ol.addOption(FCLI.makeArgOption("m", "The maximum number of items to fetch", "num"));
		ol.addOption(FCLI.makeArgOption("s", "The source page", "source"));

		return ol;
	}
}