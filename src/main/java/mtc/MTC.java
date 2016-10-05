package mtc;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.dwrap.ImageInfo;
import fastily.jwiki.util.FError;
import fastily.jwiki.util.FL;
import fastily.jwikix.tplate.Template;
import util.Toolbox;

/**
 * Business Logic for MTC. Contains shared methods, constants, and Objects.
 * 
 * @author Fastily
 *
 */
public final class MTC
{
	/**
	 * Files with these categories should not be transferred.
	 */
	private final HashSet<String> blacklist;

	/**
	 * Files must be members of at least one of the following categories to be eligible for transfer.
	 */
	private final HashSet<String> whitelist;
	/**
	 * The Wiki objects to use
	 */
	protected final Wiki enwp, com;

	/**
	 * Flag indicating whether this is a debug-mode/dry run (do not perform transfers)
	 */
	protected boolean dryRun = false;

	/**
	 * Flag indicating whether the non-free content filter is to be ignored.
	 */
	protected boolean ignoreFilter = false;

	/**
	 * Contains data for license tags
	 */
	protected TreeMap<String, String> tpMap = new TreeMap<>(new Template.TValueCmp());

	/**
	 * The DGen associated with this MTC.
	 */
	protected DGen dgen;

	/**
	 * Initializes the Wiki objects and download folders for MTC.
	 * 
	 * @param wiki A logged-in Wiki object
	 * 
	 * @throws Throwable On IO error
	 */
	protected MTC(Wiki wiki) throws Throwable
	{
		// Generate download directory
		if (Files.isRegularFile(Config.fdPath))
			FError.errAndExit(Config.fdump + " is file, please remove it so MTC can continue");
		else if (!Files.isDirectory(Config.fdPath))
			Files.createDirectory(Config.fdPath);

		// Initialize Wiki objects
		com = wiki.getWiki("commons.wikimedia.org");
		enwp = wiki.getWiki("en.wikipedia.org");

		// Process template data
		for (Map.Entry<String, String> e : Toolbox.fetchPairedConfig(enwp, Config.fullname + "/Regexes").entrySet())
		{
			String t = wiki.nss(e.getKey());
			for (String s : e.getValue().split("\\|"))
				tpMap.put(s, t);
		}

		// regexMap = new HashMap<String, Pattern>(Toolbox.fetchPairedConfig(enwp, Config.fullname +
		// "/Regexes").entrySet().stream()
		// .collect(Collectors.toMap(Map.Entry::getKey, e ->
		// Pattern.compile(TParse.makeTitleRegex(FL.toSAL(e.getValue().split("\\|")))))));
		dgen = new DGen(this);

		// Generate whitelist & blacklist
		HashMap<String, ArrayList<String>> l = MQuery.getLinksOnPage(enwp,
				FL.toSAL(Config.fullname + "/Blacklist", Config.fullname + "/Whitelist"), NS.CATEGORY);
		blacklist = new HashSet<>(l.get(Config.fullname + "/Blacklist"));
		whitelist = new HashSet<>(l.get(Config.fullname + "/Whitelist"));
	}

	/**
	 * Filters (if enabled) and resolves Commons filenames for transfer candidates
	 * 
	 * @param titles The local files to transfer
	 * @return An ArrayList of TransferObject objects.
	 */
	protected ArrayList<TransferObject> filterAndResolve(ArrayList<String> titles)
	{
		return FL.toAL(resolveFileNames(!ignoreFilter ? canTransfer(titles) : titles).entrySet().stream()
				.map(e -> new TransferObject(e.getKey(), e.getValue())));
	}

	/**
	 * Find available file names on Commons for each enwp file. The enwp filename will be returned if it is free on
	 * Commons, otherwise it will be permuted.
	 * 
	 * @param l The list of enwp files to find a Commons filename for
	 * @return The Map such that [ enwp_filename : commons_filename ]
	 */
	private HashMap<String, String> resolveFileNames(ArrayList<String> l)
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
					comFN = Toolbox.permuteFileName(enwp.nss(title));
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
	private ArrayList<String> canTransfer(ArrayList<String> titles)
	{
		ArrayList<String> l = FL.toAL(MQuery.getSharedDuplicatesOf(enwp, titles).entrySet().stream()
				.filter(e -> e.getValue().size() == 0).map(Map.Entry::getKey));
		return l.isEmpty() ? l
				: FL.toAL(MQuery.getCategoriesOnPage(enwp, l).entrySet().stream().filter(
						e -> !e.getValue().stream().anyMatch(blacklist::contains) && e.getValue().stream().anyMatch(whitelist::contains))
						.map(Map.Entry::getKey));
	}

	/**
	 * Represents a file to transfer to Commons
	 * 
	 * @author Fastily
	 *
	 */
	protected class TransferObject
	{
		/**
		 * The enwp filename
		 */
		protected final String wpFN;

		/**
		 * The commons filename and local path
		 */
		private final String comFN, localFN;

		/**
		 * The local, enwp ImageInfo objects associated with this TransferObject.
		 */
		protected ArrayList<ImageInfo> ii;

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

			String baseFN = enwp.nss(wpFN);
			localFN = Config.fdump + baseFN.hashCode() + baseFN.substring(baseFN.lastIndexOf('.'));
		}

		/**
		 * Attempts to transfer an enwp file to Commons
		 * 
		 * @return True on success.
		 */
		protected boolean doTransfer()
		{
			ii = enwp.getImageInfo(wpFN);

			String t = dgen.generate(this);

			if (dryRun)
			{
				System.out.println(t);
				return true;
			}
			else if (t != null && Toolbox.downloadFile(ii.get(0).url, localFN)
					&& com.upload(Paths.get(localFN), comFN, t, String.format(Config.tFrom, wpFN)))
				return enwp.addText(wpFN, String.format("{{subst:ncd|%s}}%n", comFN), Config.tTo, true);

			return false;
		}
	}
}