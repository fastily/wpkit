package util;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jwiki.util.FString;
import jwiki.util.FSystem;

/**
 * Read, write, and file system functions.
 * 
 * @author Fastily
 * 
 */
public class FIO
{
	/**
	 * A path matcher which will match files ok to upload to WMF wikis.
	 */
	public static final PathMatcher wmfuploadable = FileSystems.getDefault()
			.getPathMatcher("regex:(?i).+?\\.(png|gif|jpg|jpeg|xcf|mid|ogg|ogv|oga|svg|djvu|tiff|tif|pdf|webm|flac|wav)");

	/**
	 * Constructors disallowed.
	 */
	private FIO()
	{

	}

	/**
	 * Reads the lines from a file
	 * 
	 * @param p The path to the file
	 * @return The lines in the file, deliminated by newline character.
	 */
	public static ArrayList<String> readLinesFromFile(String p)
	{
		try
		{
			return new ArrayList<>(Files.readAllLines(Paths.get(p)));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	/**
	 * Gets the file name pointed to by a path object and returns it as a String. Works for both directories and files.
	 * 
	 * @param p The filename to get a name for.
	 * @return The file's name
	 */
	public static String getFileName(Path p)
	{
		return p.getFileName().toString();
	}

	/**
	 * Gets a file's extension and returns it as a String. The path MUST point to a valid file, or you'll get null.
	 * 
	 * @param p The pathname to get an extension for.
	 * @param useDot Set to true to include a filename dot.
	 * @return The file's extension, or the empty string if the file has no extension.
	 */
	public static String getExtension(Path p, boolean useDot)
	{
		if (Files.isDirectory(p))
			return null;

		String name = getFileName(p);
		int i = name.lastIndexOf('.'); // special case, file has no extension.

		return i == -1 ? "" : name.substring(i + (useDot ? 0 : 1));
	}

	/**
	 * Performs a simple line dump to file
	 * 
	 * @param path The local file path to write to
	 * @param lines The lines to dump to the file; each line will be separated by the default OS line separator.
	 */
	public static void simpleFileDump(String path, ArrayList<String> lines)
	{
		try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(path), Charset.defaultCharset(), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))
		{
			bw.write(FString.fenceMaker(FSystem.lsep, lines));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Recursively search a directory for files that match a PathMatcher.
	 * 
	 * @param root The root directory to start traversing at
	 * @param pm The PathMatcher to use
	 * @return A list of files we found.
	 */
	public static ArrayList<Path> findFiles(Path root, PathMatcher pm)
	{
		try (Stream<Path> l = Files.walk(root))
		{
			return l.filter(pm::matches).collect(Collectors.toCollection(ArrayList::new));
		}
		catch (Throwable e)
		{
			return new ArrayList<>();
		}
	}

	/**
	 * Recursively search a directory for files which can be uploaded to WMF wikis.
	 * 
	 * @param root The root directory to search. PRECONDITION: This MUST be a directory.
	 * @return A list of files that we can upload to Commons, or the empty list if we found nothing.
	 */
	public static ArrayList<Path> findFiles(Path root)
	{
		return findFiles(root, wmfuploadable);
	}
}