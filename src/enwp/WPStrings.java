package enwp;

/**
 * Common, shared static Strings.
 * 
 * @author Fastily
 *
 */
public class WPStrings
{
	/**
	 * Constructors disallowed
	 */
	private WPStrings()
	{

	}

	/**
	 * Used as part of report headers.
	 */
	protected static final String updatedAt = "This report updated at ~~~~~\n";
	
	/**
	 * Matches a date of the form dd-mmmm-yyyy.
	 */
	protected static final String DMYRegex = "\\d{1,2}? (January|February|March|April|May|June|July|August|September|October|November|December) \\d{4}?";
}