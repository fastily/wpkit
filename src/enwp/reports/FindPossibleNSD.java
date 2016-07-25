package enwp.reports;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;

import jwiki.core.NS;
import jwiki.core.Wiki;
import jwikix.core.WikiGen;
import jwikix.util.FSystem;

/**
 * Crude search for NSD files
 * 
 * @author Fastily
 *
 */
public class FindPossibleNSD
{
	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used
	 * @throws Throwable On I/O error
	 */
	public static void main(String[] args) throws Throwable
	{
		Wiki wiki = WikiGen.wg.get("FastilyBot", "en.wikipedia.org");

		HashSet<String> l = new HashSet<>(wiki.getCategoryMembers("Category:All free media", NS.FILE));

		l.removeAll(new HashSet<>(wiki.whatTranscludesHere("Template:Information", NS.FILE)));
		l.removeAll(new HashSet<>(wiki.whatTranscludesHere("Template:Self", NS.FILE)));
		l.removeAll(new HashSet<>(wiki.whatTranscludesHere("Template:PD-self", NS.FILE)));
		l.removeAll(new HashSet<>(wiki.whatTranscludesHere("Template:GFDL-self", NS.FILE)));
		l.removeAll(new HashSet<>(wiki.whatTranscludesHere("Template:GFDL-self-with-disclaimers", NS.FILE)));

		Files.write(Paths.get(FSystem.home + FSystem.psep + "PossibleNSD.txt"), l, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING);
	}
}