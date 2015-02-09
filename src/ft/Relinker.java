package ft;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jwiki.core.MBot;
import jwiki.core.Namespace;
import jwiki.core.Wiki;
import jwiki.util.FError;
import jwiki.util.FString;
import jwiki.util.Tuple;
import util.FNet;
import util.WikiGen;

/**
 * CLI utility which re-links files unlinked by CommonsDelinker
 * 
 * @author Fastily
 *
 */
public class Relinker
{
	/**
	 * Main driver
	 * 
	 * @param args Takes a single argument: the title of the file to re-link.
	 */
	public static void main(String[] args)
	{
		if (args.length != 1)
			FError.errAndExit("Usage: Relinker <filename>");

		String p = FNet.get("https://tools.wmflabs.org/commons-delinquent/index.php?action=unlink&result=1&image="
				+ FString.enc(Namespace.nss(args[0])));
		Matcher m = Pattern.compile("\\<tr\\>\\<td nowrap.+?\\</td\\>\\</tr\\>").matcher(p);

		ArrayList<Tuple<String, String>> l = new ArrayList<>();
		while (m.find())
		{
			String t = p.substring(m.start(), m.end()).replaceAll("\\<(/??tr|/td.*?)\\>", "").split("\\<td.*?\\>")[3]
					.split("href\\='//")[2];
			l.add(new Tuple<>(t.substring(t.indexOf(">") + 1, t.indexOf('<')), t.substring(0, t.indexOf("/"))));
		}

		ArrayList<PageRelink> prl = new ArrayList<>();
		for (Tuple<String, String> tx : l)
			prl.add(new PageRelink(tx));

		WikiGen.wg.get(2).submit(prl, 1);
	}

	/**
	 * Represents a page to re-link
	 * @author Fastily
	 *
	 */
	private static class PageRelink extends MBot.Task
	{
		/**
		 * A key value pair, where the first element is a title and the second is it's shorthand url.
		 */
		private Tuple<String, String> tx;

		/**
		 * Constructor.
		 * @param tx Should be (title, short_url).
		 */
		private PageRelink(Tuple<String, String> tx)
		{
			super(tx.x, null, null);
			this.tx = tx;
		}

		/**
		 * Re-links a file if it determines CommonsDelinker made the last edit.
		 */
		public boolean doJob(Wiki wiki)
		{
			try
			{
				Wiki w = wiki.getWiki(tx.y);
				return w.getRevisions(tx.x, 1, false).get(0).user.equals("CommonsDelinker") ? w.undo(tx.x,
						"Reverting CommonsDelinker") : true;
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}
}