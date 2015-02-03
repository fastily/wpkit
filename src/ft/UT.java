package ft;

import static jwiki.core.MBot.Task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import commons.CStrings;
import commons.Commons;
import jwiki.core.ColorLog;
import jwiki.core.Wiki;
import jwiki.util.FError;
import jwiki.util.FIO;
import jwiki.util.FString;
import jwiki.util.ReadFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import util.FCLI;
import util.WikiGen;

/**
 * Random file uploader. Use in conjunction with bash tools for diagnostics.
 * 
 * @author Fastily
 * 
 */
public class UT
{
	/**
	 * Upload test text.
	 */
	private static final String utt = "Recreating [[bugzilla:36587]] (i.e. [[Special:UploadStash|upload stash]] bug) & "
			+ "collecting data to log.\n{{Warning|'''Test area only!  File may be non-free.''' This is just a test"
			+ " file and any license does not apply.}}\n[[Category:Fastily Test]]";

	/**
	 * The help string for this method.
	 */
	private static final String hstring = "CC [-nr] [-help] [-h number] [-r retries] [-f] [-nd|-sd] <-t <textfile>>|<files or directories>";

	/**
	 * Flag indicating whether we should suppress deletions
	 */
	private static boolean nd;

	/**
	 * The number of times we should repeat in event of failure
	 */
	private static int repeats;	
	
	/**
	 * Main driver.
	 * 
	 * @param args Prog args
	 * @throws ParseException eh?
	 */
	public static void main(String[] args) throws ParseException
	{
		CommandLine l = FCLI.gnuParse(makeOptList(), args, hstring);

		if (l.hasOption('f'))
			Commons.categoryNuke(WikiGen.wg.get("Fastily"), "Fastily Test", CStrings.ur, false);

		nd = l.hasOption("nd") || l.hasOption("sd");
		repeats = Integer.parseInt(l.getOptionValue('r', "1"));

		ArrayList<CCW> ccwl = l.hasOption('t') ? generateCCW(new ReadFile(l.getOptionValue('t')).l) : generateCCW(Arrays
				.asList(l.getArgs()));

		
		ArrayList<String> ml = Task.toString(WikiGen.wg.get("FastilyClone").submit(ccwl, Integer.parseInt(l.getOptionValue('h', "1"))));
		if (ml.size() > 0)
			FIO.dumpToFile("./CCfails.txt", true, ml);
	}

	/**
	 * Grabs the files we're planning to upload and converts them to CCW objects. If nothing is passed into
	 * <tt>paths</tt>, search the classpath for files to uplaod.
	 * 
	 * @param paths The paths of the file(s) or directories to search for files.
	 * @return A list of uploadable files we found.
	 */
	private static ArrayList<CCW> generateCCW(List<String> paths)
	{
		HashSet<Path> sl = new HashSet<>();
		for (String s : paths)
		{
			Path t = Paths.get(s);
			if (Files.isDirectory(t))
				sl.addAll(FIO.findFiles(t));
			else
				sl.add(t);
		}

		if (sl.isEmpty())
			FError.errAndExit("No paths provided, program exiting.");

		ArrayList<CCW> x = new ArrayList<>();
		for (Path p : sl)
			x.add(new CCW(p));
		return x;
	}

	/**
	 * Makes a list of options for us.
	 * 
	 * @return The list of options.
	 */
	private static Options makeOptList()
	{
		Options ol = FCLI.makeDefaultOptions();

		ol.addOption("nd", false, "Surpress deletion after upload");
		ol.addOption("sd", false, "Alias of '-nd'");
		ol.addOption("f", false, "Nuke 'Category:Fastily Test' and exit.  Overrides other options");

		ol.addOption(FCLI.makeArgOption("h", "Sets the number of threads of execution", "#threads"));
		ol.addOption(FCLI.makeArgOption("r", "Number of times to repeat in event of failure", "#retries"));
		ol.addOption(FCLI.makeArgOption("t", "Select files to upload from a text file", "textfile"));

		return ol;
	}

	/**
	 * Inner class implementing WAction so we can use this class with MBot.
	 * 
	 * @author Fastily
	 * 
	 */
	private static class CCW extends Task
	{
		/**
		 * The path pointing to the file to upload
		 */
		private Path p;

		/**
		 * Constructor, takes a path pointing to the file to upload.
		 * 
		 * @param p The path pointing to the file to upload.
		 */
		private CCW(Path p)
		{
			super(p.toAbsolutePath().toString(), utt, "");
			this.p = p;
		}

		/**
		 * Performs upload & delete
		 */
		public boolean doJob(Wiki wiki)
		{
			for (int i = 0; i < repeats; i++)
			{
				String fn = "File:" + FString.generateRandomFileName(p);
				ColorLog.fyi(String.format("(%d/%d): Upload '%s' -> '%s'", i + 1, repeats, FIO.getFileName(p), fn));

				if (wiki.upload(p, fn, text, " "))
				{
					if (!nd)
						WikiGen.wg.get("Fastily").delete(fn, CStrings.ur);
					return true;
				}
			}
			return false;
		}
	}
}