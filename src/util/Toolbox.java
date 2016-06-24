package util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jwiki.core.ColorLog;
import jwiki.core.Req;
import jwiki.core.Wiki;
import jwikix.util.StrTool;

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
	 * Parses a config page with key-value pairs. Empty lines and lines starting with '&gt;' are ignored. Key-value pairs
	 * should be split by <code>;</code>, one pair per line.
	 * 
	 * @param wiki The Wiki object to use
	 * @param title The title of the config page to parse
	 * @return A HashMap with the parsed pairs.
	 */
	public static HashMap<String, String> fetchConfig(Wiki wiki, String title)
	{
		return new HashMap<>(Stream.of(wiki.getPageText(title).split("\n")).filter(s -> !s.startsWith("<") && !s.isEmpty())
				.map(s -> s.split(";", 2)).collect(Collectors.toMap(a -> a[0], a -> a[1])));
	}

	/**
	 * Downloads a file and saves it to disk.
	 * 
	 * @param u The URL to download from
	 * @param localpath The local path to save the file at.
	 * @return True on success.
	 */
	public static boolean downloadFile(URL u, String localpath)
	{
		ColorLog.fyi("Downloading a file to " + localpath);
		
		byte[] bf = new byte[1024 * 512]; // 512kb buffer.
		int read;
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
		}
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
	 * Permutes a filename by adding a random number to the end before the file extension. PRECONDITION: <code>fn</code>
	 * is a valid filename with an extension, of the format (e.g. blahblah.jpg)
	 * 
	 * @param fn The base filename to permute
	 * @return The permuted filename
	 */
	public static String permuteFileName(String fn)
	{
		return StrTool.insertAt(fn, " " + rand.nextInt(), fn.lastIndexOf('.'));
	}
}