package mtc;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import jwiki.core.ColorLog;
import jwiki.core.NS;
import jwiki.util.FL;
import jwikix.util.WikiGen;
import mtc.MTC.TransferObject;
import util.FCLI;

/**
 * Command line interface for MTC.
 * 
 * @author Fastily
 *
 */
public final class CMTC
{
	/**
	 * The MTC instance for this class.
	 */
	private static MTC mtc;

	/**
	 * Main driver
	 * 
	 * @param args Program args
	 */
	public static void main(String[] args) throws Throwable
	{
		CommandLine l = FCLI.gnuParse(makeOptList(), args, "MTC [-help] [-f] [-d] [<titles|user|cat>]");

		// Do initial logins, and generate MTC regexes
		mtc = new MTC(WikiGen.wg.get("FastilyClone", "commons.wikimedia.org"));
		mtc.dryRun = l.hasOption('d');

		if (l.hasOption('f'))
			mtc.ignoreFilter = true;

		ArrayList<String> fl = new ArrayList<>();
		for (String s : l.getArgs())
		{
			NS ns = mtc.enwp.whichNS(s);
			if (ns.equals(NS.FILE))
				fl.add(s);
			else if (ns.equals(NS.CATEGORY))
				fl.addAll(mtc.enwp.getCategoryMembers(s, NS.FILE));
			else if (ns.equals(NS.TEMPLATE))
				fl.addAll(mtc.enwp.whatTranscludesHere(s, NS.FILE));
			else
				fl.addAll(mtc.enwp.getUserUploads(s));
		}

		procList(fl);
	}

	/**
	 * Attempts to move files to Commons
	 * 
	 * @param titles The titles to try and move.
	 */
	private static void procList(ArrayList<String> titles)
	{
		ArrayList<TransferObject> tl = mtc.filterAndResolve(titles);
		AtomicInteger i = new AtomicInteger();
		int total = tl.size();

		ArrayList<String> fails = FL.toAL(tl.stream().filter(to -> {
			ColorLog.fyi(String.format("Processing item %d of %d", i.incrementAndGet(), total));
			return !to.doTransfer();
		}).map(to -> to.wpFN));

		System.out.printf("Task complete, with %d failures: %s%n", fails.size(), fails);
	}

	/**
	 * Makes the list of CLI options.
	 * 
	 * @return The list of Command line options.
	 */
	private static Options makeOptList()
	{
		Options ol = FCLI.makeDefaultOptions();
		ol.addOption("f", "Force (ignore filter) file transfer(s)");
		ol.addOption("d", "Activate dry run/debug mode (does not transfer files)");
		return ol;
	}

}