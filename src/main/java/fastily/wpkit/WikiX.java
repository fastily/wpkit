package fastily.wpkit;

import java.time.format.DateTimeFormatter;

/**
 * Miscellaneous Wiki-related routines.
 * 
 * @author Fastily
 *
 */
public final class WikiX
{
	/**
	 * A date formatter for UTC times.
	 */
	public static final DateTimeFormatter iso8601dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * Constructors disallowed
	 */
	private WikiX()
	{

	}
}