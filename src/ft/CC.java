package ft;

import static ft.Core.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import jwiki.commons.CStrings;
import jwiki.core.Logger;
import jwiki.core.Wiki;
import jwiki.mbot.MBot;
import jwiki.mbot.WAction;
import jwiki.util.FCLI;
import jwiki.util.FIO;
import jwiki.util.FString;
import jwiki.util.ReadFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Random file uploader. Use in conjunction with bash tools for diagnostics.
 * 
 * @author Fastily
 * 
 */
public class CC
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
	private static final String hstring = "CC [-nr] [-help] [-h number] [-r retries] [-f] [-nd|-sd] [-t <textfile>|<files or directories>]";
	
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
		CommandLine l = init(args, makeOptList(), hstring);
		
		if (l.hasOption('f'))
			com.nukeFastilyTest(true);
		
		nd = l.hasOption("nd") || l.hasOption("sd");
		repeats = Integer.parseInt(l.getOptionValue('r', "1"));
		
		CCW[] ccwl;
		if (l.hasOption('t'))
			ccwl = generateCCW(new ReadFile(l.getOptionValue('t')).getList());
		else
			ccwl = generateCCW(l.getArgs());
		
		String[] ml = WAction.convertToString(new MBot(user, Integer.parseInt(l.getOptionValue('h', "1"))).start(ccwl));
		if(ml.length > 0)
			FIO.dumpToFile("./CCfails.txt", ml);
	}
	
	/**
	 * Grabs the files we're planning to upload and converts them to CCW objects. If nothing is passed into
	 * <tt>paths</tt>, search the classpath for files to uplaod.
	 * 
	 * @param paths The paths of the file(s) or directories to search for files.
	 * @return A list of uploadable files we found.
	 */
	private static CCW[] generateCCW(String... paths)
	{
		HashSet<Path> sl = new HashSet<Path>();
		for (String s : paths)
		{
			Path t = Paths.get(s);
			if (Files.isDirectory(t))
				sl.addAll(FIO.findFiles(t));
			else
				sl.add(t);
		}
		
		if (sl.isEmpty())
			sl.addAll(FIO.findFiles(Paths.get(".")));
		
		ArrayList<CCW> x = new ArrayList<CCW>();
		for (Path p : sl)
			x.add(new CCW(p));
		return x.toArray(new CCW[0]);
	}
	
	/**
	 * Makes a list of options for us.
	 * 
	 * @return The list of options.
	 */
	private static Options makeOptList()
	{
		Options ol = new Options();
		
		ol.addOption("nd", false, "Surpress deletion after upload");
		ol.addOption("sd", false, "Alias of '-nd'");
		ol.addOption("f", false, "Nuke 'Category:Fastily Test' and exit.  Overrides other options");
		
		ol.addOption(FCLI.makeArgOption("h", "Sets the number of threads of execution", "#threads"));
		ol.addOption(FCLI.makeArgOption("r", "Number of times to repeat in event of failure", "#retries"));
		ol.addOption(FCLI.makeArgOption("t", "Select files to upload from a text file", "<textfile>"));
		
		return ol;
	}
	
	/**
	 * Inner class implementing WAction so we can use this class with MBot.
	 * 
	 * @author Fastily
	 * 
	 */
	private static class CCW extends WAction
	{
		/**
		 * The path pointing to the file to upload
		 */
		private Path p;
		
		/**
		 * Constructor, takes a path pointing to the file to upload. 
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
				Logger.fyi(wiki, String.format("(%d/%d): Upload '%s' -> '%s'", i + 1, repeats, FIO.getFileName(p), fn));
				
				if (wiki.upload(p, fn, text, " "))
				{
					if (!nd)
						admin.delete(fn, CStrings.ur);
					return true;
				}
			}
			return false;
		}
	}
}