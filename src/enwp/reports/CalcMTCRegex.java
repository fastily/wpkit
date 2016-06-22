package enwp.reports;

import java.util.ArrayList;
import java.util.HashSet;

import jwiki.core.MQuery;
import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.FString;
import jwiki.util.Tuple;
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

		HashSet<String> rawL = new HashSet<>(wiki.getLinksOnPage("Wikipedia:MTC!/Regexes/IncludeAlso", NS.TEMPLATE));
		rawL.addAll(TallyLics.comtpl);

		String x = "<!-- This is a bot-generated regex library for MTC!, please don't touch, thanks! -->\n<pre>\n";
		for (Tuple<String, ArrayList<String>> e : FL.mapToList(MQuery.linksHere(wiki, true, new ArrayList<>(rawL))))
		{
			e.y.add(e.x);
			x += String.format("%s;%s%n", e.x, FString.pipeFence(WTool.stripNamespaces(wiki, e.y)));
		}

		x += "</pre>";

		wiki.edit("Wikipedia:MTC!/Regexes", x, "Update report");
	}
}