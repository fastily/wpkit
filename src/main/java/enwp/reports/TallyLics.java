package enwp.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import ctools.util.Toolbox;
import enwp.WPStrings;
import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;

/**
 * Counts up free license tags and checks if a Commons counterpart exists.
 * 
 * @author Fastily
 *
 */
public class TallyLics
{
	/**
	 * The Wiki objects to use
	 */
	private static Wiki enwp = Toolbox.getFastilyBot(), com = Toolbox.getCommons(enwp);

	/**
	 * The title to post the report to.
	 */
	private static String reportPage = String.format("User:%s/Free License Tags", enwp.whoami());
	
	/**
	 * The blacklist of pages to omit from the final report
	 */
	private static ArrayList<String> bl = enwp.getLinksOnPage(reportPage + "/Ignore", NS.TEMPLATE);

	/**
	 * A list of en.wikipedia free license templates
	 */
	protected static ArrayList<String> enwptpl = FL.toAL(enwp.getLinksOnPage(reportPage + "/Sources", NS.CATEGORY).stream()
			.flatMap(cat -> enwp.getCategoryMembers(cat, NS.TEMPLATE).stream()).filter(s -> !bl.contains(s) && !s.endsWith("/sandbox")));

	/**
	 * The list of Commons templates with the same name as the enwp templates.
	 */
	protected static HashSet<String> comtpl = new HashSet<>(MQuery.exists(com, true, enwptpl));

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		Collections.sort(enwptpl);
		
		String dump = WPStrings.updatedAt + "\n{| class=\"wikitable sortable\" style=\"margin-left: auto; margin-right: auto;width:100%;\" \n! # !! Name !! Commons? \n";
		
		int i = 0;
		for (String s : enwptpl)
			dump += String.format("|-%n|%d ||{{Tlx|%s}} ||[[c:%s|%b]] %n", ++i, enwp.nss(s), s, comtpl.contains(s));

		dump += "|}";

		enwp.edit(reportPage, dump, "Updating report");
	}
}