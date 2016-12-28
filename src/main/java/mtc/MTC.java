package mtc;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import ctools.util.Toolbox;
import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FError;
import fastily.jwiki.util.FL;
import fastily.jwikix.core.TParse;
import fastily.jwikix.tplate.Template;

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
	 * Regex matching Copy to Commons templates.
	 */
	protected final String mtcRegex;
	
	/**
	 * Flag indicating whether this is a debug-mode/dry run (do not perform transfers)
	 */
	protected boolean dryRun = false;

	/**
	 * Flag indicating whether the non-free content filter is to be ignored.
	 */
	protected boolean ignoreFilter = false;

	/**
	 * Flag indicating whether the Commons category tracking transfers should be used.
	 */
	protected boolean useTrackingCat = true;
	
	/**
	 * Contains data for license tags
	 */
	protected TreeMap<String, String> tpMap = new TreeMap<>(new Template.TValueCmp());

	/**
	 * Initializes the Wiki objects and download folders for MTC.
	 * 
	 * @param wiki A logged-in Wiki object
	 * 
	 * @throws Throwable On IO error
	 */
	public MTC(Wiki wiki) throws Throwable
	{
		// Generate download directory
		if (Files.isRegularFile(Config.fdPath))
			FError.errAndExit(Config.fdump + " is file, please remove it so MTC can continue");
		else if (!Files.isDirectory(Config.fdPath))
			Files.createDirectory(Config.fdPath);

		// Initialize Wiki objects
		com = wiki.getWiki("commons.wikimedia.org");
		enwp = wiki.getWiki("en.wikipedia.org");

		mtcRegex = TParse.makeTemplateRegex(enwp, "Template:Copy to Wikimedia Commons");
		
		// Process template data
		for (Map.Entry<String, String> e : Toolbox.fetchPairedConfig(enwp, Config.fullname + "/Regexes").entrySet())
		{
			String t = wiki.nss(e.getKey());
			for (String s : e.getValue().split("\\|"))
				tpMap.put(s, t);
		}

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
	protected ArrayList<TransferFile> filterAndResolve(ArrayList<String> titles)
	{
		return FL.toAL(resolveFileNames(!ignoreFilter ? canTransfer(titles) : titles).entrySet().stream()
				.map(e -> new TransferFile(e.getKey(), e.getValue(), this)));
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
	 * @param titles The title to check
	 * @return True if the file can <ins>probably</ins> be transfered to Commons.
	 */
	public ArrayList<String> canTransfer(ArrayList<String> titles)
	{
		ArrayList<String> l = FL.toAL(MQuery.getSharedDuplicatesOf(enwp, titles).entrySet().stream()
				.filter(e -> e.getValue().size() == 0).map(Map.Entry::getKey));
		return l.isEmpty() ? l
				: FL.toAL(MQuery.getCategoriesOnPage(enwp, l).entrySet().stream().filter(
						e -> !e.getValue().stream().anyMatch(blacklist::contains) && e.getValue().stream().anyMatch(whitelist::contains))
						.map(Map.Entry::getKey));
	}
}