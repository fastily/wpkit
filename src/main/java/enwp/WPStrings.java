package enwp;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
	 * Wiki-text message stating that a bot did not nominate any files for deletion.
	 */
	public static final String botNote = "\n{{subst:User:FastilyBot/BotNote}}";

	/**
	 * Generates an {@code Template:Ncd}} template for a bot user.
	 * 
	 * @param wiki The bot username to use
	 * @return The template.
	 */
	public static String makeNCDBotTemlpate(String user)
	{
		return String.format("{{Now Commons|%%s|date=%s|bot=%s}}%n",
				DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.now(ZoneOffset.UTC)), user);
	}
}