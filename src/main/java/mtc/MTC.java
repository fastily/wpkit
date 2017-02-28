package mtc;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import ctools.util.Toolbox;
import ctools.util.WikiX;
import enwp.WTP;
import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.FSystem;

/**
 * Business Logic for MTC. Contains shared methods, constants, and Objects.
 * 
 * @author Fastily
 *
 */
public final class MTC
{
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
	protected TreeMap<String, String> tpMap = new TreeMap<>(WikiX.tpParamCmp);

	/**
	 * Files with these categories should not be transferred.
	 */
	protected HashSet<String> blacklist;

	/**
	 * Files must be members of at least one of the following categories to be eligible for transfer.
	 */
	protected HashSet<String> whitelist;

	/**
	 * Templates which indicate that a file is own work.
	 */
	protected HashSet<String> selflist;
	
	/**
	 * The Wiki objects to use
	 */
	protected Wiki enwp, com;

	/**
	 * Initializes the Wiki objects and download folders for MTC.
	 * 
	 * @param enwp A logged-in Wiki object, set to {@code en.wikipedia.org}
	 * 
	 * @throws Throwable On IO error
	 */
	public MTC(Wiki enwp) throws Throwable
	{
		// Initialize Wiki objects
		this.enwp = enwp;
		com = Toolbox.getCommons(enwp);

		// Generate whitelist & blacklist
		HashMap<String, ArrayList<String>> l = MQuery.getLinksOnPage(enwp,
				FL.toSAL(Config.fullname + "/Blacklist", Config.fullname + "/Whitelist", Config.fullname + "/Self"));
		blacklist = new HashSet<>(l.get(Config.fullname + "/Blacklist"));
		whitelist = new HashSet<>(l.get(Config.fullname + "/Whitelist"));
		selflist = FL.toSet(l.get(Config.fullname + "/Self").stream().map(enwp::nss));

		// Generate download directory
		if (Files.isRegularFile(Config.fdPath))
			FSystem.errAndExit(Config.fdump + " is file, please remove it so MTC can continue");
		else if (!Files.isDirectory(Config.fdPath))
			Files.createDirectory(Config.fdPath);

		mtcRegex = WTP.mtc.getRegex(enwp);

		// Process template data
		Toolbox.fetchPairedConfig(enwp, Config.fullname + "/Regexes").forEach((k, v) -> {
			String t = enwp.nss(k);
			for (String s : v.split("\\|"))
				tpMap.put(s, t);
		});
	}

	/**
	 * Filters (if enabled) and resolves Commons filenames for transfer candidates
	 * 
	 * @param titles The local files to transfer
	 * @return An ArrayList of TransferObject objects.
	 */
	protected ArrayList<TransferFile> filterAndResolve(ArrayList<String> titles)
	{
		ArrayList<TransferFile> l = new ArrayList<>();
		resolveFileNames(!ignoreFilter ? canTransfer(titles) : titles).forEach((k, v) -> l.add(new TransferFile(k, v, this)));
		return l;
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
		MQuery.exists(com, l).forEach((k, v) -> {
			if (!v)
				m.put(k, k);
			else
			{
				String comFN;
				do
				{
					comFN = Toolbox.permuteFileName(k);
				} while (com.exists(comFN)); // loop until available filename is found

				m.put(k, comFN);
			}
		});

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
		ArrayList<String> l = new ArrayList<>();
		MQuery.getSharedDuplicatesOf(enwp, titles).forEach((k, v) -> {
			if (v.size() == 0)
				l.add(k);
		});

		ArrayList<String> rl = new ArrayList<>();
		MQuery.getCategoriesOnPage(enwp, l).forEach((k, v) -> {
			if (!v.stream().anyMatch(blacklist::contains) && v.stream().anyMatch(whitelist::contains))
				rl.add(k);
		});

		return rl;
	}
}