package fastily.wpkit.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Stream;

import fastily.jwiki.core.ColorLog;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.FSystem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Miscellaneous custom functions common to my scripts/bots.
 * 
 * @author Fastily
 *
 */
public final class Toolbox
{
	/**
	 * Random number generator.
	 * 
	 * @see #permuteFileName(String)
	 */
	private static final Random rand = new Random();

	/**
	 * Formats dates as as year month day
	 * 
	 * @see #dateAsYMD(TemporalAccessor)
	 */
	private static DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyy MMMM d");

	/**
	 * Formats dates as as day month year
	 * 
	 * @see #dateAsDMY(TemporalAccessor)
	 */
	private static DateTimeFormatter DMY = DateTimeFormatter.ofPattern("d MMMM yyyy");

	/**
	 * Constructors disallowed
	 */
	private Toolbox()
	{

	}

	/**
	 * Gets the specified WikiGen user at en.wikipedia.org.
	 * 
	 * @param user The user to get a Wiki object for
	 * @return A Wiki object, or null on error
	 */
	private static Wiki getUser(String user)
	{
		return WikiGen.wg.get(user, "en.wikipedia.org");
	}

	/**
	 * Gets a Wiki (from WikiGen) for Fastily at en.wikipedia.org.
	 * 
	 * @return A Wiki object, or null on error
	 */
	public static Wiki getFastily()
	{
		return getUser("Fastily");
	}

	/**
	 * Gets a Wiki (from WikiGen) for FSock at en.wikipedia.org.
	 * 
	 * @return A Wiki object, or null on error
	 */
	public static Wiki getFSock()
	{
		return getUser("FSock");
	}

	/**
	 * Gets a Wiki (from WikiGen) for FastilyBot at en.wikipedia.org.
	 * 
	 * @return A Wiki object, or null on error
	 */
	public static Wiki getFastilyBot()
	{
		return getUser("FastilyBot");
	}

	/**
	 * Derives a Wiki from {@code wiki} with the domain set to {@code commons.wikimedia.org}.
	 * 
	 * @param wiki The Wiki object to derive a new Commons Wiki from.
	 * @return A Wiki pointing to Commons, or null on error.
	 */
	public static Wiki getCommons(Wiki wiki)
	{
		return wiki.getWiki("commons.wikimedia.org");
	}

	/**
	 * Generates a ZonedDateTime of the current date and time.
	 * 
	 * @return The current date and time, in UTC.
	 */
	public static ZonedDateTime getUTCofNow()
	{
		return ZonedDateTime.now(ZoneOffset.UTC);
	}

	/**
	 * Formats a TemporalAccessor as a year-month-date. ex: {@code 2017 February 6}
	 * 
	 * @param d The TemporalAccessor to format.
	 * @return A String derived from the TemporalAccessor in YMD format.
	 */
	public static String dateAsYMD(TemporalAccessor d)
	{
		return YMD.format(d);
	}

	/**
	 * Formats a TemporalAccessor as a year-month-date. ex: {@code 6 February 2017}
	 * 
	 * @param d The TemporalAccessor to format.
	 * @return A String derived from the TemporalAccessor in YMD format.
	 */
	public static String dateAsDMY(TemporalAccessor d)
	{
		return DMY.format(d);
	}

	/**
	 * Fetches the contents of a page, splits them by new line, and strips empty Strings and Strings starting with '&gt;'
	 * 
	 * @param wiki The Wiki object to use
	 * @param title The title of the config page to parse
	 * @return An unclosed String with the specified data.
	 */
	private static Stream<String> fetchRawConfig(Wiki wiki, String title)
	{
		return Stream.of(wiki.getPageText(title).split("\n")).filter(s -> !s.startsWith("<") && !s.isEmpty());
	}

	/**
	 * Parses a config page with key-value pairs. Empty lines and lines starting with '&gt;' are ignored. Key-value pairs
	 * should be split by {@code ;}, one pair per line.
	 * 
	 * @param wiki The Wiki object to use
	 * @param title The title of the config page to parse
	 * @return A HashMap with the parsed pairs.
	 */
	public static HashMap<String, String> fetchPairedConfig(Wiki wiki, String title)
	{
		return FL.toHM(
				Stream.of(wiki.getPageText(title).split("\n")).filter(s -> !s.startsWith("<") && !s.isEmpty()).map(s -> s.split(";", 2)),
				a -> a[0], a -> a[1]);
	}

	/**
	 * Parses a config page with a list of items. Empty lines and lines starting with '&gt;' are ignored.
	 * 
	 * @param wiki The Wiki object to use
	 * @param title The title of the config page to parse
	 * @return A HashSet with the list of items.
	 */
	public static HashSet<String> fetchSimpleConfig(Wiki wiki, String title)
	{
		return FL.toSet(fetchRawConfig(wiki, title));
	}

	/**
	 * Downloads a file and saves it to disk.
	 * @param client The OkHttpClient to use perform network connections with.
	 * @param u The url to download from
	 * @param localpath The local path to save the file at.
	 * @return True on success.
	 */
	public static boolean downloadFile(OkHttpClient client, String u, String localpath)
	{
		ColorLog.fyi("Downloading a file to " + localpath);

		byte[] bf = new byte[1024 * 512]; // 512kb buffer.
		int read;
		try (Response r = client.newCall(new Request.Builder().url(u).get().build()).execute();
				OutputStream out = Files.newOutputStream(Paths.get(localpath)))
		{
			InputStream in = r.body().byteStream();
			while ((read = in.read(bf)) > -1)
				out.write(bf, 0, read);

			return true;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Checks the version String of a program with the version String of the server. PRECONDITION: <code>local</code> and
	 * <code>ext</code> ONLY contain numbers and '.' characters.
	 * 
	 * @param local The version String of the program. (e.g. 0.2.1)
	 * @param minVersion The version String of the server. (e.g. 1.3.2)
	 * @return True if the version of the local String is greater than or equal to the server's version String.
	 */
	public static boolean versionCheck(String local, String minVersion)
	{
		try
		{
			return Integer.parseInt(local.replace(".", "")) >= Integer.parseInt(minVersion.replace(".", ""));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Permutes a filename by adding a random number to the end before the file extension. PRECONDITION: {@code fn} is a
	 * valid filename with an extension, of the format (e.g. blahblah.jpg)
	 * 
	 * @param fn The base filename to permute
	 * @return The permuted filename
	 */
	public static String permuteFileName(String fn)
	{
		return insertAt(fn, " " + rand.nextInt(), fn.lastIndexOf('.'));
	}

	/**
	 * Inserts a String into a String. PRECONDITION: {@code index} is a valid index for {@code s}
	 * 
	 * @param s The string to insert into
	 * @param insert The String to be inserted
	 * @param index The index to insert {@code insert} at. The original character at this index will be shifted down one
	 *           slot to make room for {@code insert}
	 * @return The modified String.
	 */
	public static String insertAt(String s, String insert, int index)
	{
		return new StringBuffer(s).insert(index, insert).toString();
	}

	/**
	 * Generates a Wiki-text ready, wiki-linked, unordered list from a list of titles.
	 * 
	 * @param header A header/lead string to apply at the beginning of the returned String.
	 * @param titles The titles to use
	 * @param doEscape Set as true to escape titles. i.e. adds a <code>:</code> before each link so that files and
	 *           categories are properly escaped and appear as links.
	 * @return A String with the titles as a linked, unordered list, in Wiki-text.
	 */
	public static String listify(String header, Collection<String> titles, boolean doEscape)
	{
		String fmtStr = "* [[" + (doEscape ? ":" : "") + "%s]]" + FSystem.lsep;

		String x = "" + header;
		for (String s : titles)
			x += String.format(fmtStr, s);

		return x;
	}

	/**
	 * Fetch a simple report from fastilybot's toollabs dumps.
	 * 
	 * @param wiki The Wiki object to use
	 * @param report The name of the report, without the {@code .txt} extension.
	 * @return A String Array with each item in the report, or the empty Array if something went wrong.
	 */
	public static String[] fetchLabsReportList(Wiki wiki, String report)
	{
		try
		{
			Response r = wiki.apiclient.client.newCall(
					new Request.Builder().url(String.format("https://tools.wmflabs.org/fastilybot/r/%s.txt", report)).get().build())
					.execute();

			if (r.isSuccessful())
				return r.body().string().split("\n");
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return new String[0];
	}

	/**
	 * Fetch a simple report from fastilybot's toollabs dumps. Auto-formats each item in the report by adding a
	 * {@code File:} prefix and by replacing underscores with spaces.
	 * 
	 * @param wiki The Wiki object to use
	 * @param report The name of the report, without the {@code .txt} extension.
	 * @return A String HashSet with each item in the report, or the empty HashSet if something went wrong.
	 */
	public static HashSet<String> fetchLabsReportListAsFiles(Wiki wiki, String report)
	{
		return FL.toSet(Arrays.stream(fetchLabsReportList(wiki, report)).map(s -> "File:" + s.replace('_', ' ')));
	}
}