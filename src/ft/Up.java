package ft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import util.FCLI;
import jwiki.core.Wiki;
import jwiki.mbot.MBot;
import jwiki.mbot.WAction;
import jwiki.util.FIO;
import jwiki.util.FString;
import static ft.Core.*;

/**
 * Uploads my files to Commons. Accepts directories as arguments; Commons-acceptable files in the directories will be
 * uploaded with the parent directory name as the title, along with the current date. Categories will be set to the name
 * of the parent directory. Program will also output a file named <tt>Up fails.txt</tt> in the working directory if we
 * failed to upload any file(s).
 * 
 * @author Fastily
 * 
 */
public class Up
{
	/**
	 * The format string for file description pages.
	 */
	private static final String descbase = "=={{int:filedesc}}==\n{{Information\n|Description=%s\n|Source={{own}}\n"
			+ "|Date=%s\n|Author=~~~\n|Permission=\n|other_versions=\n}}\n\n=={{int:license-header}}==\n"
			+ "{{self|Cc-by-sa-4.0}}\n\n[[Category:%s]]\n[[Category:Files by Fastily]]";

	/**
	 * Formats dates for use in file description pages.
	 */
	private static final DateTimeFormatter descdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * Today's date to use in the title of uploaded files.
	 */
	private static final String titledate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

	/**
	 * Mapping construct tracking how many times we've seen files of a particular directory. Used to provide incrementing
	 * titles for files on Wiki.
	 */
	private static final HashMap<String, Integer> tracker = new HashMap<String, Integer>();

	/**
	 * Main driver.
	 * 
	 * @param args Prog args
	 */
	public static void main(String[] args)
	{
		CommandLine l = init(args, makeOptList(), "Up [-t <threads>]");

		try
		{
			String[] fails = WAction.convertToString(new MBot(admin, Integer.parseInt(l.getOptionValue('t', "1")))
					.start(generateUploadItem(args)));
			if (fails.length > 0)
				FIO.dumpToFile("./Up fails.txt", true, fails);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	private static Options makeOptList()
	{
		Options ol = new Options();
		ol.addOption(FCLI.makeArgOption("t", "Sets the maximum number of concurrent threads uploading", "threads"));

		return ol;
	}

	/**
	 * Generates upload items based on the arguments passed in (directory and file paths). Recursively searches
	 * directories. Auto-resolves duplicates.
	 * 
	 * @param args The arguments to sort/parse
	 * @return The list of UploadItems we found.
	 * @throws IOException If i/o error.
	 */
	private static UploadItem[] generateUploadItem(String[] args) throws IOException
	{
		HashSet<Path> l = new HashSet<Path>();
		for (String s : args)
		{
			Path temp = Paths.get(s).toAbsolutePath();

			if (!Files.exists(temp)) // precondition: files must exist
				continue;
			else if (Files.isDirectory(temp)) // recusrive search directories for goodies
				l.addAll(FIO.findFiles(temp));
			else if (FIO.canUploadToWMF(temp)) // individual files ok
				l.add(temp);
		}

		ArrayList<UploadItem> ul = new ArrayList<UploadItem>();
		for (Path p : l)
		{
			String titlebase = FString.capitalize(FIO.getFileName(p.getParent()));

			int i = 1;
			if (tracker.containsKey(titlebase))
				tracker.put(titlebase, new Integer(i = tracker.get(titlebase).intValue() + 1)); // f yeah one liners
			else
				tracker.put(titlebase, new Integer(i));

			// assemble date and title strings.
			String filedate = LocalDateTime.ofInstant(Instant.ofEpochMilli(Files.getLastModifiedTime(p).toMillis()),
					ZoneId.systemDefault()).format(descdf);
			String filename = String.format("File:%s %d %s.%s", titlebase, i, titledate, FIO.getExtension(p, false));

			ul.add(new UploadItem(p, filename, String.format(descbase, titlebase, filedate, titlebase)));
		}

		return ul.toArray(new UploadItem[0]);
	}

	/**
	 * Represents an item to upload.
	 * 
	 * @author Fastily
	 * 
	 */
	private static class UploadItem extends WAction
	{
		/**
		 * The path pointing to the file we'd like to upload
		 */
		private Path p;

		/**
		 * The title to upload to
		 */
		private String uploadTo;

		/**
		 * Constructor, takes path, description page, and title to upload to.
		 * 
		 * @param p The path pointing to the file we'd like to upload
		 * @param title The title to upload to on wiki, including the "File:" prefix.
		 * @param text The text to use on the description page.
		 */
		private UploadItem(Path p, String title, String text)
		{
			super(p.toAbsolutePath().toString(), text, null);
			uploadTo = title;
			this.p = p;
		}

		/**
		 * Performs the upload.
		 */
		public boolean doJob(Wiki wiki)
		{
			return wiki.upload(p, uploadTo, text, " ");
		}
	}
}