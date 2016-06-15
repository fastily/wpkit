package enwp.reports;

import java.util.ArrayList;

import jwiki.core.MQuery;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.Tuple;
import jwikix.util.TParse;
import jwikix.util.WTool;
import jwikix.util.WikiGen;

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
		Wiki wiki = WikiGen.wg.get("FastilyBot", "en.wikipedia.org");

		String x = "<!-- This is a bot-generated regex library for MTC!, please don't touch, thanks! -->\n<pre>\n";
		for (Tuple<String, ArrayList<String>> e : FL.mapToList(MQuery.linksHere(wiki, true, new ArrayList<>(TallyLics.comtpl))))
		{
			e.y.add(e.x);
			x += String.format("%s;%s%n", e.x, TParse.makeTitleRegex(WTool.stripNamespaces(wiki, e.y)));
		}

		x += "</pre>";

		wiki.edit("Wikipedia:MTC!/Regexes", x, "Update report");
	}
}