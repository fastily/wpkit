package up;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.stream.Stream;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.FString;
import fastily.jwikix.core.WikiGen;

/**
 * Simple Wikimedia Commons Uploader
 * 
 * @author Fastily
 *
 */
public final class Up
{
	/**
	 * The user to login as
	 */
	private static final String user = "Fastily";

	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = WikiGen.wg.get(user, "commons.wikimedia.org");

	/**
	 * The regex matching file extensions which can be uploaded to Commons
	 */
	private static final String extRegex = "(?i).+?\\.(" + FString.pipeFence(wiki.getAllowedFileExts()) + ")";

	/**
	 * The String template for file description pages
	 */
	private static final String infoT = "=={{int:filedesc}}==\n{{Information\n|description=%s\n|date=%s\n|source={{Own}}\n|"
			+ "author=~~~\n|permission=\n|other versions=\n}}\n\n=={{int:license-header}}==\n{{Self|Cc-by-sa-4.0}}\n\n[[Category:%s]]\n[[Category:Files by %s]]";

	/**
	 * The formatter to use for <code>date=</code> dates
	 */
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * The template String for on-Wiki file names
	 */
	private static final String fnBase = String.format("File:%%s %%d %s.%%s", DateTimeFormatter.ISO_DATE.format(LocalDate.now()));

	/**
	 * Logs failed uploads
	 */
	private static final HashSet<String> fails = new HashSet<>();

	/**
	 * Main driver
	 * 
	 * @param args Program arguments. PRECONDITION: These are valid directory paths
	 * @throws Throwable I/O Error
	 */
	public static void main(String[] args) throws Throwable
	{
		for (Path d : FL.toAL(Stream.of(args).map(Paths::get).filter(Files::isDirectory)))
			procDir(d);

		if (!fails.isEmpty())
		{
			System.out.println("Failed on:");
			for (String s : fails)
				System.out.println("\t* " + s);
		}
	}

	/**
	 * Looks for files in a directory to upload
	 * 
	 * @param d The directory to look at
	 * @throws Throwable I/O Error
	 */
	private static void procDir(Path d) throws Throwable
	{
		int i = 0;
		String name = d.getFileName().toString();

		for (Path f : FL.toSet(Files.list(d).filter(f -> Files.isRegularFile(f) && f.toString().matches(extRegex))))
		{
			String text = String.format(infoT, name,
					dtf.format(LocalDateTime.ofInstant(Files.getLastModifiedTime(f).toInstant(), ZoneId.of("UTC"))), name, user);
			String fn = String.format(fnBase, name, ++i, getExt(f));

			if (!wiki.upload(f, fn, text, ""))
				fails.add(f.toString());
		}
	}

	/**
	 * Extracts the file extension
	 * 
	 * @param f The file to get an extension for
	 * @return The file extension, without a '.'
	 */
	private static String getExt(Path f)
	{
		String x = f.getFileName().toString();
		return x.substring(x.lastIndexOf('.') + 1).toLowerCase();
	}
}