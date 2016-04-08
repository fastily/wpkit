package enwp.tools;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import jwiki.core.ColorLog;
import jwiki.core.MQuery;
import jwiki.core.NS;
import jwiki.core.Req;
import jwiki.core.WTask;
import jwiki.core.Wiki;
import jwiki.util.FError;
import jwiki.util.FL;
import jwiki.util.FString;
import jwikix.util.FCLI;
import jwikix.util.StrTool;
import jwikix.util.WTool;
import jwikix.util.WikiGen;

/**
 * CLI utility to assist with transfer of files from enwp to Commons.
 * 
 * @author Fastily
 *
 */
public final class MTC
{
	/**
	 * Files with these categories should not be transferred.
	 */
	private static final ArrayList<String> blacklist = FL.toSAL(
			"Category:Wikipedia files on Wikimedia Commons for which a local copy has been requested to be kept",
			"Category:Wikipedia files not suitable for Commons", "Category:Wikipedia files of no use beyond Wikipedia",
			"Category:All non-free media", "Category:All Wikipedia files with unknown source",
			"Category:All Wikipedia files with unknown copyright status", "Category:Candidates for speedy deletion",
			"Category:All possibly unfree Wikipedia files", "Category:Wikipedia files for discussion", "Category:All free in US media",
			"Category:Files deleted on Wikimedia Commons", "Category:All Wikipedia files with the same name on Wikimedia Commons",
			"Category:All Wikipedia files with a different name on Wikimedia Commons",
			"Category:Wikipedia files with disputed copyright information", "Category:Items pending OTRS confirmation of permission",
			"Category:Wikipedia files with unconfirmed permission received by OTRS by date", "Category:Images in non-image formats",
			"Category:All media requiring a US status confirmation", "Category:Files nominated for deletion on Wikimedia Commons",
			"Category:Wikipedia files moved to Wikimedia Commons which could not be deleted",
			"Category:Images published abroad that are in the public domain in the United States", "Category:Duplicate Wikipedia files",
			"Category:Pre-1996 PD in home country US public domain images");

	/**
	 * Files must be members of at least one of the following categories to be eligible for transfer.
	 */
	private static final ArrayList<String> whitelist = FL.toSAL("Category:All free media", "Category:Self-published work",
			"Category:GFDL files with disclaimers");
	/**
	 * The Wiki objects
	 */
	private static Wiki enwp, com;

	/**
	 * The directory pointing to the location for file downloads
	 */
	private static final String fdump = "filedump/";

	/**
	 * The Path object pointing to <code>fdump</code>.
	 */
	private static final Path fdPath = Paths.get(fdump);

	/**
	 * Creates the regular expression matching Copy to Wikimedia Commons
	 */
	private static String tRegex;

	/**
	 * Flag indicating whether this is a dry run (do not perform transfers)
	 */
	private static boolean dryRun;

	/**
	 * Main driver
	 * 
	 * @param args Program args
	 */
	public static void main(String[] args) throws Throwable
	{
		CommandLine l = FCLI.gnuParse(makeOptList(), args, "MTC [-help] [-u <user>|-f <file>|-c <cat>] [<titles>]");

		if (Files.isRegularFile(fdPath))
			FError.errAndExit(fdump + " is file, please remove it so MTC can continue");
		else if (!Files.isDirectory(fdPath))
			Files.createDirectory(fdPath);

		com = WikiGen.wg.get("FastilyClone", "commons.wikimedia.org");
		enwp = com.getWiki("en.wikipedia.org");
		tRegex = WTool.makeTemplateRegex(enwp, "Template:Copy to Wikimedia Commons");

		dryRun = l.hasOption('d');

		if (l.hasOption('u'))
			procList(enwp.getUserUploads(l.getOptionValue('u')));
		else if (l.hasOption('f'))
			procList(Files.readAllLines(Paths.get(l.getOptionValue('f'))));
		else if (l.hasOption('c'))
			procList(enwp.getCategoryMembers(l.getOptionValue('c'), NS.FILE));
		else
			procList(FL.toSAL(l.getArgs()));
	}

	/**
	 * Attempts to move files to Commons
	 * 
	 * @param titles The titles to try and move.
	 */
	private static void procList(List<String> titles)
	{
		ArrayList<String> fails = new ArrayList<>(), l = canTransfer(new ArrayList<>(titles));
		HashMap<String, String> fileNames = resolveFileNames(l);

		int cnt = 0, total = l.size();
		TransferObject to;
		for (String s : l)
		{
			ColorLog.fyi(String.format("Processing item %d of %d", ++cnt, total));
			if (!(to = new TransferObject(s, fileNames.get(s))).doTransfer())
				fails.add(to.wpFN);
		}

		System.out.printf("Task complete, with %d failures: %s%n", fails.size(), fails);
	}

	/**
	 * Find available file names on Commons for each enwp file. The enwp filename will be returned if it is free on
	 * Commons, otherwise it will be permuted.
	 * 
	 * @param l The list of enwp files to find a Commons filename for
	 * @return The Map such that [ enwp_filename : commons_filename ]
	 */
	private static HashMap<String, String> resolveFileNames(ArrayList<String> l)
	{
		HashMap<String, String> m = new HashMap<>();
		for (Map.Entry<String, Boolean> e : MQuery.exists(com, l).entrySet())
		{
			String title = e.getKey();

			if (!e.getValue())
				m.put(title, title);
			else
			{
				String comFN;
				do
				{
					comFN = StrTool.permuteFileName(enwp.nss(title));
				} while (com.exists(comFN)); // loop until available filename is found

				m.put(title, comFN);
			}
		}

		return m;
	}

	/**
	 * Performs checks to determine if a file can be transfered to Commons.
	 * 
	 * @param title The title to check
	 * @return True if the file can <ins>probably</ins> be transfered to Commons.
	 */
	private static ArrayList<String> canTransfer(ArrayList<String> titles)
	{
		ArrayList<String> l = FL.toAL(MQuery.getSharedDuplicatesOf(enwp, titles).entrySet().stream()
				.filter(e -> e.getValue().size() == 0).map(Map.Entry::getKey));
		return l.isEmpty() ? l
				: FL.toAL(MQuery.getCategoriesOnPage(enwp, l).entrySet().stream()
						.filter(e -> !StrTool.arraysIntersect(e.getValue(), blacklist) && StrTool.arraysIntersect(e.getValue(), whitelist))
						.map(Map.Entry::getKey));
	}

	/**
	 * Makes the list of CLI options.
	 * 
	 * @return The list of Command line options.
	 */
	private static Options makeOptList()
	{
		Options ol = FCLI.makeDefaultOptions();
		ol.addOptionGroup(FCLI.makeOptGroup(FCLI.makeArgOption("u", "Transfer eligible files uploaded by a user", "user"),
				FCLI.makeArgOption("f", "Transfer titles listed in a text file", "file"),
				FCLI.makeArgOption("c", "Transfer files in category", "cat")));
		ol.addOption("d", "Activate dry run/debug mode (does not transfer files)");
		return ol;
	}

	/**
	 * Represents a file to transwiki
	 * 
	 * @author Fastily
	 *
	 */
	private static class TransferObject
	{
		/**
		 * The URL to post to.
		 */
		private static final String url = "http://tools.wmflabs.org/commonshelper/index.php";

		/**
		 * The template text to post to the wmflabs tool.
		 */
		private static final String posttext = "language=en&project=wikipedia&image=%s&newname=&ignorewarnings=1&doit=Get+text&test=%%2F";

		/**
		 * Matches vomit that CommonsHelper doesn't strip.
		 */
		private static final String uselessT = String.format("(?si)\\{\\{(%s)\\}\\}\n?",
				FString.pipeFence("Green", "Red", "Yesno", "Center", "Own", "Section link", "Trademark", "Bad JPEG", "OTRS permission",
						"Spoken article entry", "PD\\-BritishGov", "Convert", "Cc\\-by\\-sa", "Infosplit", "Cite book", "Trim", "Legend",
						"Hidden begin", "Hidden end"));

		/**
		 * Matches GFDL-disclaimers templates
		 */
		private static final Pattern gfdlDiscl = Pattern.compile("(?i)\\{\\{GFDL\\-user\\-(w|en)\\-(with|no)\\-disclaimers");

		/**
		 * String which is a regex tht matches caption sections
		 */
		private static final String captionRegexStr = "(?si)\n?\\=\\=\\s*?(Caption).+?\\|\\}";

		/**
		 * Matches caption sections
		 */
		private static final Pattern captionRegex = Pattern.compile(captionRegexStr);

		/**
		 * The of enwp usages of InfoSplit
		 */
		private static final ArrayList<String> infoSplitUses = enwp.whatTranscludesHere("Template:Infosplit");

		/**
		 * Matches infosplit templates
		 */
		private static final Pattern infoSplitT = Pattern.compile("(?si)\\{\\{(infosplit).+?\\}\\}");

		/**
		 * The enwp, commons, basefilename (just basename), and local path to the file.
		 */
		private String wpFN, comFN, baseFN, localFN;

		/**
		 * The text which goes on the file description page of the Commons copy
		 */
		private String t;

		/**
		 * Constructor, creates a TransferObject
		 * 
		 * @param wpFN The enwp title to transfer
		 * @param comFN The commons title to transfer to
		 */
		private TransferObject(String wpFN, String comFN)
		{
			this.comFN = comFN;
			this.wpFN = wpFN;
			baseFN = enwp.nss(wpFN);
			localFN = fdump + baseFN;
		}

		/**
		 * Downloads this object's enwp file and transfers it to Commons.
		 * 
		 * @return True on success.
		 */
		private boolean doTransfer()
		{
			try
			{
				generateText();

				if (dryRun)
				{
					System.out.println(t);
					return true;
				}

				if (WTask.downloadFile(wpFN, localFN, enwp)
						&& com.upload(Paths.get(localFN), comFN, t, String.format("Transferred from [[w:%s|enwp]]", wpFN)))
					return enwp.edit(wpFN, String.format("{{subst:ncd|%s}}%n", comFN) + enwp.getPageText(wpFN).replaceAll(tRegex, ""),
							"ncd");

			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}

			return false;
		}

		/**
		 * Generates a file description page for a file which will be moved to Commons
		 * 
		 * @throws Throwable Network error
		 */
		private void generateText() throws Throwable
		{
			ColorLog.fyi("Downloading text for " + wpFN);

			// generate raw file desc text for Commons
			String rawhtml = FString
					.inputStreamToString(Req.genericPOST(new URL(url), null, Req.urlenc, String.format(posttext, FString.enc(baseFN))));
			t = rawhtml.substring(rawhtml.indexOf("{{Info"), rawhtml.indexOf("</textarea>"));

			// cleanup text
			t = t.replaceAll(uselessT, "");
			t = t.replaceAll("(?si)\\{\\{(\\QCc-by-sa-3.0-migrated\\E|Copy to Commons|Do not move to Commons).*?\\}\\}\n?", "");
			t = t.replaceAll("\\Q<!--\\E.*?\\Q-->\\E\n?", "");
			t = t.replaceAll("(?i)\\|(Permission)\\=.*?\n", "|Permission=\n");
			t = t.replaceAll("(?i)\\|(Source)\\=(Transferred from).*?\n", "|Source={{Transferred from|en.wikipedia}}\n");
			t = t.replace("{{subst:Unc}}", "").replaceAll("__NOTOC__\n?", "");
			t = t.replace("&times;", "×");
			t = t.replace("Original uploader was {{user at project", "{{Original uploader");

			if (!t.contains("int:filedesc"))
				t = "== {{int:filedesc}} ==\n" + t;

			// fix malformed GFDL-disclaimers templates
			Matcher m = gfdlDiscl.matcher(t);
			if (m.find())
				try
				{
					t = String.format("%s|1=%s%s", t.substring(0, m.end()), enwp.getRevisions(wpFN, 1, true, null, null).get(0).user,
							t.substring(m.end()));
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}

			// fix malformed caption headers
			if ((m = captionRegex.matcher(t)).find())
			{
				String capt = m.group(); // copy caption section
				t = t.replaceAll(captionRegexStr, ""); // strip
				t += capt; // dump it at the end
			}

			// Fixing missing {{Infosplit}}
			if (infoSplitUses.contains(wpFN) && (m = infoSplitT.matcher(enwp.getPageText(wpFN))).find())
			{
				String isplit = m.group();
				t = StrTool.insertAt(t, "\n" + isplit, t.indexOf("== {{int:license-header}} ==") - 2);
			}
		}
	}
}