package enwp;

import java.util.ArrayList;

import jwiki.core.Wiki;
import jwikix.util.WTool;
import jwikix.util.WikiGen;

/**
 * Looks for GFDL-tagged files which may be missing source information.
 * 
 * @author Fastily
 *
 */
public class FindNSD
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		Wiki wiki = WikiGen.wg.get("FastilyBot", "en.wikipedia.org");
		ArrayList<String> gfdlDiscl = wiki.whatTranscludesHere("Template:GFDL-with-disclaimers");

		gfdlDiscl.removeAll(wiki.whatTranscludesHere("Template:Self"));
		gfdlDiscl.removeAll(wiki.whatTranscludesHere("Template:Information"));
		gfdlDiscl.removeAll(wiki.whatTranscludesHere("Template:Now Commons"));

		System.out.println(gfdlDiscl.size());
		wiki.edit("User:FastilyBot/Sandbox1", WTool.listify("", gfdlDiscl, true), "update report");
	}
}