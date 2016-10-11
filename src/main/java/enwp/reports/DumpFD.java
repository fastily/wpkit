package enwp.reports;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import util.FCLI;
import util.Toolbox;

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
	 * The selection query offset and maximum number of items. These are off by default.
	 */
	private static int offset = 0, max = -1;

	/**
	 * Main driver
	 * 
	 * @param args Program args
	 */
	public static void main(String[] args)
	{
		CommandLine cl = FCLI.gnuParse(makeOpts(), args, "DumpFD [-m] [-o] [-s]");
		Wiki wiki = Toolbox.getFastilyClone();

		if (cl.hasOption('m'))
			max = Integer.parseInt(cl.getOptionValue('m'));
		if (cl.hasOption('o'))
			offset = Integer.parseInt(cl.getOptionValue('o'));
		if (cl.hasOption('s'))
			srcMTC = cl.getOptionValue('s');

		ArrayList<String> l = null;

		switch (wiki.whichNS(srcMTC).v)
		{
			case 0: // user, but sometimes w/o namespace.
			case 2: // user
				l = prepWS(wiki.getUserUploads(srcMTC));
				break;
			case 10: // template transclusions
				l = prepWS(wiki.whatTranscludesHere(srcMTC, NS.FILE));
				break;
			case 14: // category members
				l = prepWS(wiki.getCategoryMembers(srcMTC, NS.FILE));
				break;
			default:
				throw new IllegalArgumentException(srcMTC + " is not a valid source for MTC files");
		}

		String x = "Report Generated @ ~~~~~\n{| class=\"wikitable sortable\" style=\"margin-left: auto; margin-right: auto;\"\n! Title \n! File \n! Desc\n|-\n";

		for (Map.Entry<String, String> e : MQuery.getPageText(wiki, l).entrySet())
			x += String.format("| [[:%s]]%n| [[%s|center|200px]]%n| <pre>%s</pre>%n|-%n", e.getKey(), e.getKey(), e.getValue());

		x += "|}\n";

		wiki.edit(String.format("User:%s/MTCSources", wiki.whoami()), x, "Updating report");
	}

	/**
	 * Prepares the working set of files. Applies offsets and truncates the working set as specified by the user
	 * 
	 * @param l The raw input list
	 * @return The updated working set of files.
	 */
	private static ArrayList<String> prepWS(ArrayList<String> l)
	{
		return max == -1 ? l : new ArrayList<>(l.subList(offset, max));
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
		ol.addOption(FCLI.makeArgOption("o", "Offset the result set by the specified amount", "offset"));
		ol.addOption(FCLI.makeArgOption("s", "The source page", "source"));

		return ol;
	}
}