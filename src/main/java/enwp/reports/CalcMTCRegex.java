package enwp.reports;

import java.util.ArrayList;
import java.util.HashSet;

import ctools.util.Toolbox;
import ctools.util.WikiX;
import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.Tuple;

/**
 * Pre-computes regexes for MTC!
 * 
 * @author Fastily
 *
 */
public class CalcMTCRegex
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used
	 */
	public static void main(String[] args)
	{
		Wiki wiki = Toolbox.getFastilyBot();

		HashSet<String> rawL = new HashSet<>(wiki.getLinksOnPage("Wikipedia:MTC!/Regexes/IncludeAlso", NS.TEMPLATE));
		rawL.addAll(TallyLics.comtpl);

		String x = "<!-- This is a bot-generated regex library for MTC!, please don't touch, thanks! -->\n<pre>\n";
		for (Tuple<String, ArrayList<String>> e : FL.mapToList(MQuery.linksHere(wiki, true, new ArrayList<>(rawL))))
		{
			e.y.add(e.x);
			x += String.format("%s;%s%n", e.x, FL.pipeFence(WikiX.stripNamespaces(wiki, e.y)));
		}

		x += "</pre>";

		wiki.edit("Wikipedia:MTC!/Regexes", x, "Update report");
	}
}