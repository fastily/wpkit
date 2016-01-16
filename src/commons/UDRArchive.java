package commons;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jwiki.extras.WikiGen;
import jwiki.core.Wiki;

/**
 * A tiny COM:UD archiving bot.
 * 
 * @author Fastily
 * 
 */
public class UDRArchive
{
	/**
	 * Main driver.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		Wiki wiki = WikiGen.wg.get("ArchiveBot");
		String target = "Commons:Undeletion requests/Current requests";
		String text = wiki.getPageText(target);
		Matcher m = Pattern.compile("(?si)\\s*?\\{\\{(udelh)\\}\\}.+?\\{\\{(udelf)\\}\\}\\s*?").matcher(text);

		String dump = "";
		int cnt = 0;
		while (m.find())
		{
			dump += text.substring(m.start(), m.end());
			cnt++;
		}

		String summary = "Archiving %d thread(s) %s [[%s]]";
		String archive = "Commons:Undeletion requests/Archive/"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

		if (cnt == 0) // if there were no threads to archive, exit
			return;

		wiki.edit(archive, wiki.exists(archive) ? wiki.getPageText(archive) + dump : dump,
				String.format(summary, cnt, "from", target));
		wiki.edit(target, m.replaceAll(""), String.format(summary, cnt, "to", archive));
	}
}