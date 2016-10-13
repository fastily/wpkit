package enwp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import enwp.bots.DDNotifier;
import enwp.bots.DateNowCommons;
import enwp.bots.FFDNotifier;
import enwp.bots.FindCommonsFFD;
import enwp.bots.FindDelComFFD;
import enwp.bots.FindKeptComFFD;
import enwp.bots.FindLicConflict;
import enwp.bots.FlagOI;
import enwp.bots.ManageMTC;
import enwp.bots.RemoveBadMTC;
import enwp.bots.UnflagOI;
import enwp.reports.CalcMTCRegex;
import enwp.reports.FindBrokenSPI;
import enwp.reports.FindOrphanedXfD;
import enwp.reports.FindUntaggedDD;
import enwp.reports.TallyLics;
import util.FCLI;

/**
 * CLI interface which makes it easy to launch enwp bots/reports
 * 
 * @author Fastily
 *
 */
public final class BMgr
{
	/**
	 * Format String for errors caused by bad arguments
	 */
	private static final String badNumberFmt = "'%s' is not a valid %s number%n";

	/**
	 * Main driver
	 * 
	 * @param args Program arguments. Additional arguments will be passed to tasks/reports.
	 * @throws Throwable If one of the called bots/reports had an error.
	 */
	public static void main(String[] args) throws Throwable
	{
		CommandLine cl = FCLI.gnuParse(makeOpts(), args, "BotMgr [-r <report number> |-b <task number>] [--help] [Task/Report Args...]");

		String[] pArgs = cl.getArgs();
		if (cl.hasOption('b'))
			switch (Integer.parseInt(cl.getOptionValue('b')))
			{
				case 1:
					ManageMTC.main(pArgs);
					break;
				case 2:
					RemoveBadMTC.main(pArgs);
					break;
				case 3:
					FindBrokenSPI.main(pArgs);
					break;
				case 4:
					UnflagOI.main(pArgs);
					break;
				case 5:
					FindLicConflict.main(pArgs);
					break;
				case 6:
					DDNotifier.main(pArgs);
					break;
				case 7:
					FindCommonsFFD.main(pArgs);
					break;
				case 8:
					FindDelComFFD.main(pArgs);
					break;
				case 9:
					FindKeptComFFD.main(pArgs);
					break;
				case 10:
					FlagOI.main(pArgs);
					break;
				case 11:
					DateNowCommons.main(pArgs);
					break;
				case 12:
					FFDNotifier.main(pArgs);
					break;
				default:
					System.err.printf(badNumberFmt, cl.getOptionValue('b'), "task");
			}
		else if (cl.hasOption('r'))
			switch (Integer.parseInt(cl.getOptionValue('r')))
			{
				case 1:
					FindUntaggedDD.main(pArgs);
					break;
				case 2:
					FindOrphanedXfD.main(pArgs);
					break;
				case 3:
					TallyLics.main(pArgs);
					break;
				case 4:
					CalcMTCRegex.main(pArgs);
					break;
				default:
					System.err.printf(badNumberFmt, cl.getOptionValue('r'), "report");
			}
		else
			System.out.println("Invalid argument, please run with --help for usage instructions");
	}

	/**
	 * Creates the Options list for the program
	 * 
	 * @return A list of Options
	 */
	private static Options makeOpts()
	{
		Options ol = FCLI.makeDefaultOptions();
		ol.addOptionGroup(FCLI.makeOptGroup(FCLI.makeArgOption("b", "Triggers a bot task to be run", "Task number"),
				FCLI.makeArgOption("r", "Triggers a report task to be run", "Report number")));

		return ol;
	}
}