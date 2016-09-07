package enwp.reports;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import jwikix.util.FSystem;
import util.Toolbox;

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
		Wiki wiki = Toolbox.getFastilyBot();

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