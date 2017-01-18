package ctools.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fastily.jwiki.core.ColorLog;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.FSystem;
import fastily.jwiki.util.WikiGen;
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
	 * Gets a Wiki (from WikiGen) for FastilyClone at en.wikipedia.org.
	 * 
	 * @return A Wiki object, or null on error
	 */
	public static Wiki getFastilyClone()
	{
		return getUser("FastilyClone");
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
	 * @param wiki The Wiki object to derive a new Commons Wiki from.
	 * @return A Wiki pointing to Commons, or null on error.
	 */
	public static Wiki getCommons(Wiki wiki)
	{
		return wiki.getWiki("commons.wikimedia.org");
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
	 * should be split by <code>;</code>, one pair per line.
	 * 
	 * @param wiki The Wiki object to use
	 * @param title The title of the config page to parse
	 * @return A HashMap with the parsed pairs.
	 */
	public static HashMap<String, String> fetchPairedConfig(Wiki wiki, String title)
	{
		return new HashMap<>(Stream.of(wiki.getPageText(title).split("\n")).filter(s -> !s.startsWith("<") && !s.isEmpty())
				.map(s -> s.split(";", 2)).collect(Collectors.toMap(a -> a[0], a -> a[1])));
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
	 * 
	 * @param u The URL to download from
	 * @param localpath The local path to save the file at.
	 * @return True on success.
	 */
	public static boolean downloadFile(OkHttpClient client, String u, String localpath)
	{
		ColorLog.fyi("Downloading a file to " + localpath);

		byte[] bf = new byte[1024 * 512]; // 512kb buffer.
		int read;
		try(Response r = client.newCall(new Request.Builder().url(u).get().build()).execute(); OutputStream out = Files.newOutputStream(Paths.get(localpath)))
		{
			InputStream in = r.body().byteStream();
			while ((read = in.read(bf)) > -1)
				out.write(bf, 0, read);
			
			return true;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		
		return false;
		

		/*int read;
		try (InputStream in = Req.genericGET(u, null); OutputStream out = Files.newOutputStream(Paths.get(localpath)))
		{
			while ((read = in.read(bf)) > -1)
				out.write(bf, 0, read);

			return true;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}*/
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
	 * Permutes a filename by adding a random number to the end before the file extension. PRECONDITION: {@code fn}
	 * is a valid filename with an extension, of the format (e.g. blahblah.jpg)
	 * 
	 * @param fn The base filename to permute
	 * @return The permuted filename
	 */
	public static String permuteFileName(String fn)
	{
		return StrTool.insertAt(fn, " " + rand.nextInt(), fn.lastIndexOf('.'));
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
	 * Generates a Wiki-text ready, wiki-linked, unordered list from a list of titles.
	 * 
	 * @param header A header/lead string to apply at the beginning of the returned String.
	 * @param titles The titles to use
	 * @param doEscape Set as true to escape titles. i.e. adds a <code>:</code> before each link so that files and
	 *           categories are properly escaped and appear as links.
	 * @return A String with the titles as a linked, unordered list, in Wiki-text.
	 */
	public static String listify(String header, Stream<String> titles, boolean doEscape)
	{
		return listify(header, FL.toAL(titles), doEscape);
	}

	/**
	 * Determine if a set of link(s) has existed on a page over a given time period.
	 * 
	 * @param wiki The Wiki object to use
	 * @param title The title to query
	 * @param l The list of link(s) to look for in the history of <code>title</code>.
	 * @param start The time to start looking at (inclusive). Optional - set null to disable.
	 * @param end The time to stop the search at (exclusive). Optional - set null to disable.
	 * @return A list of link(s) that were found at some point in the page's history.
	 */
	public static ArrayList<String> detLinksInHist(Wiki wiki, String title, ArrayList<String> l, Instant start, Instant end)
	{
		ArrayList<String> texts = FL.toAL(wiki.getRevisions(title, -1, false, start, end).stream().map(r -> r.text));
		return FL.toAL(l.stream().filter(s -> texts.stream().noneMatch(t -> t.matches("(?si).*?\\[\\[:??(\\Q" + s + "\\E)\\]\\].*?"))));
	}
}