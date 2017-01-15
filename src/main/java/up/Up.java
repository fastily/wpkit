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
import fastily.jwiki.util.WikiGen;

/**
 * Simple Wikimedia Commons Uploader
 * 
 * @author Fastily
 *
 */
public final class Up
{
	/**
	 * The Wiki object to use
	 */
	private static final Wiki wiki = WikiGen.wg.get("Fastily", "commons.wikimedia.org");

	/**
	 * The regex matching file extensions which can be uploaded to Commons
	 */
	private static final String extRegex = "(?i).+?\\.(" + FL.pipeFence(wiki.getAllowedFileExts()) + ")";

	/**
	 * The String template for file description pages
	 */
	private static final String infoT = "=={{int:filedesc}}==\n{{Information\n|description=%s\n|date=%s\n|source={{Own}}\n|"
			+ "author=~~~\n|permission=\n|other versions=\n}}\n\n=={{int:license-header}}==\n{{Self|Cc-by-sa-4.0}}\n\n[[Category:%s]]\n[[Category:Files by %s]]";

	/**
	 * The formatter to use for {@code date=} dates
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
		Stream.of(args).map(Paths::get).filter(Files::isDirectory).forEach(d -> {
			int i = 0;
			String name = d.getFileName().toString();

			try
			{
				for (Path f : FL.toSet(Files.list(d).filter(f -> Files.isRegularFile(f) && f.toString().matches(extRegex))))
					if (!wiki.upload(f, String.format(fnBase, name, ++i, getExt(f)),
							String.format(infoT, name,
									dtf.format(LocalDateTime.ofInstant(Files.getLastModifiedTime(f).toInstant(), ZoneId.of("UTC"))), name,
									wiki.whoami()),
							""))
						fails.add(f.toString());
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		});

		if (!fails.isEmpty())
		{
			System.out.println("Failed on:");
			for (String s : fails)
				System.out.println("\t* " + s);
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