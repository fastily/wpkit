package mtc;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configurable, frequently used constant Strings for MTC.
 * 
 * @author Fastily
 *
 */
public class Config
{
	/**
	 * The directory pointing to the location for file downloads
	 */
	protected static final String fdump = "mtcfiles/";

	/**
	 * The Path object pointing to <code>fdump</code>.
	 */
	protected static final Path fdPath = Paths.get(fdump);

	/**
	 * The URL to post to.
	 */
	protected static final String chURL = "http://tools.wmflabs.org/commonshelper/index.php";

	/**
	 * The template text to post to the wmflabs tool.
	 */
	protected static final String chPOSTfmt = "language=en&project=wikipedia&image=%s&newname=&ignorewarnings=1&doit=Get+text&test=%%2F";

	/**
	 * Basic wikitext link to MTC!. For use in edit summaries.
	 */
	protected static final String mtcLink = "([[Wikipedia:MTC!|MTC!]])";

	/**
	 * Edit summary for files uploaded to Commons
	 */
	protected static final String tFrom = "Transferred from [[w:%s|en.wikipedia]] ([[w:Wikipedia:MTC!|MTC!]])";

	/**
	 * Edit summary for files transferred from Commons
	 */
	protected static final String tTo = "Transferred to Commons " + mtcLink;

	/**
	 * Constructors disallowed.
	 */
	private Config()
	{

	}
}