package util;

/**
 * Common, shared static Strings.
 * 
 * @author Fastily
 *
 */
public final class WPStrings
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
	public static final String updatedAt = "This report updated at ~~~~~\n";
	
	/**
	 * Matches a date of the form dd-mmmm-yyyy.
	 */
	public static final String DMYRegex = "\\d{1,2}? (January|February|March|April|May|June|July|August|September|October|November|December) \\d{4}?";
	
	/**
	 * Message for users stating that a bot did not nominate any files for deletion.
	 */
	public static final String notBotNom = "\n<span style=\"color:red;font-weight:bold;\">ATTENTION</span>: This is an automated, [[Wikipedia:Bots|BOT]]-generated message.  "
			+ "This bot DID NOT nominate any file(s) for deletion; please refer to the [[Help:Page history|page history]] of each individual file "
			+ "for details. Thanks, ~~~~";
}