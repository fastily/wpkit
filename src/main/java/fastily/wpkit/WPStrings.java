package fastily.wpkit;

import java.time.format.DateTimeFormatter;

import fastily.wpkit.util.Toolbox;

/**
 * Common, shared static Strings.
 * 
 * @author Fastily
 *
 */
public final class WPStrings
{
	/**
	 * Wiki-text message stating that a bot did not nominate any files for deletion.
	 */
	public static final String botNote = "\n{{subst:User:FastilyBot/BotNote}}";

	/**
	 * Matches a date of the form dd-mmmm-yyyy.
	 */
	public static final String DMYRegex = "\\d{1,2}? (January|February|March|April|May|June|July|August|September|October|November|December) \\d{4}?";

	/**
	 * Summary for speedy deletion criterion g8 - talk page
	 */
	public static final String csdG8talk = "[[WP:CSD#G8|G8]]: [[Help:Talk page|Talk page]] of a deleted or non-existent page";
	
	
	/**
	 * A date formatter for UTC times.
	 */
	public static final DateTimeFormatter iso8601dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * Used as part of report headers.
	 */
	public static final String updatedAt = "This report updated at ~~~~~\n";

	/**
	 * Constructors disallowed
	 */
	private WPStrings()
	{
	
	}

	/**
	 * Generates an {@code Template:Ncd} template for a bot user.
	 * 
	 * @param user The bot username to use
	 * @return The template.
	 */
	public static String makeNCDBotTemplate(String user)
	{
		return String.format("{{Now Commons|%%s|date=%s|bot=%s}}%n",
				DateTimeFormatter.ISO_LOCAL_DATE.format(Toolbox.getUTCofNow()), user);
	}
}