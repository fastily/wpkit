package commons;

import java.util.ArrayList;

import jwiki.core.ColorLog;
import jwiki.core.MBot;
import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FError;
import jwiki.util.FL;
import jwiki.util.Tuple;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import util.FCLI;
import util.StrTool;
import jwiki.extras.WikiGen;

/**
 * Command line program which globally replaces a file with a specified string. You MUST have a Unified Account if you
 * want this to work properly (it may still work if you don't, but I make no promises)
 * 
 * @author Fastily
 *
 */
public class GlobalReplace
{
	/**
	 * Main driver. Used for CLI only
	 * 
	 * @param args Program args. Specify -help for help.
	 */
	public static void main(String[] args)
	{
		CommandLine l = FCLI.gnuParse(makeOptList(), args, "GlobalReplace -f <old> -t <new> -r <rsn>");

		if (!l.hasOption('f') || !l.hasOption('t'))
			FError.errAndExit("Illegal params: you must specify -f and -t!  Exiting.");

		Wiki wiki = WikiGen.wg.get(2);
		wiki.submit(makeRItem(wiki, l.getOptionValue('f'), l.getOptionValue('t'), l.getOptionValue('r')), 1);
	}

	/**
	 * Make a list of CLI options for us.
	 * 
	 * @return The list of Command line options.
	 */
	private static Options makeOptList()
	{
		Options ol = FCLI.makeDefaultOptions();
		ol.addOption(FCLI.makeArgOption("f", "The title to replace, excluding namespace", "old"));
		ol.addOption(FCLI.makeArgOption("t", "The text to replace the old title with, excluding namespace", "new"));
		ol.addOption(FCLI.makeArgOption("r", "An optional edit summary", "rsn"));
		return ol;
	}

	/**
	 * Create RItems from Global Usage representing individual replacements.
	 * 
	 * @param wiki The wiki object to use
	 * @param old The file to replace, excluding "File:" namespace
	 * @param replacement The text to perform replacements with
	 * @param optSum An optional edit summary. Disable with null.
	 * @return A list of RItems as specified.
	 */
	protected static ArrayList<RItem> makeRItem(Wiki wiki, String old, String replacement, String optSum)
	{
		ArrayList<RItem> l = new ArrayList<>();
		String regex = StrTool.makePageTitleRegex(wiki.nss(old));
		String sum = String.format("%s → %s using [[%%sCommons:GlobalReplace|GR 0.4]]%s", old, replacement, optSum != null
				&& !optSum.isEmpty() ? ": " + optSum : "");
		for (Tuple<String, String> t : FL.mapToList(wiki.globalUsage(wiki.convertIfNotInNS(old, NS.FILE))))
			l.add(new RItem(t.x, regex, wiki.nss(replacement), sum, t.y));
		return l;
	}

	/**
	 * Represents an individual replacement task
	 * 
	 * @author Fastily
	 *
	 */
	protected static class RItem extends MBot.Task
	{
		/**
		 * Replacement regex, replacement text.
		 */
		private String regex, replacement;

		/**
		 * The short-style domain to edit at.
		 */
		protected String domain;

		/**
		 * Constructor
		 * 
		 * @param title The remote title to edit
		 * @param regex The replacement regex
		 * @param replacement The replacement text
		 * @param summary The edit summary - auto-excludes 'C:' prefix-prefix if we're editing locally.
		 * @param domain The domain to edit in - shorthand
		 */
		private RItem(String title, String regex, String replacement, String summary, String domain)
		{
			super(title, null, String.format(summary, domain.equals("commons.wikimedia.org") ? "" : "C:"));
			this.regex = regex;
			this.replacement = replacement;
			this.domain = domain;
		}

		/**
		 * Performs the replacement. Does not do anything if nothing was replaced or if we're in a blacklisted domain.
		 */
		public boolean doJob(Wiki wiki)
		{
			Wiki wx = wiki.getWiki(domain);
			switch (wx.whichNS(title).v)
			// acceptable namespaces
			{
				case 0: // main
				case 6: // file
				case 10: // template
				case 14: // category
					return wx.replaceText(title, regex, replacement, summary);
				default:
					ColorLog.fyi(title + " is not in a whitelisted domain.  Skip.");
					return true;
			}
		}
	}
}