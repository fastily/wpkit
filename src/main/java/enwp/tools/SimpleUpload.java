package enwp.tools;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import ctools.util.FCLI;
import ctools.util.Toolbox;
import ctools.util.WikiX;
import fastily.jwiki.core.Wiki;

/**
 * Simple utility to upload files to Commons.
 * 
 * @author Fastily
 *
 */
public class SimpleUpload
{
	/**
	 * The Wiki object to use
	 */
	private static Wiki wiki;

	/**
	 * Matches filenames that may be uploaded to {@code wiki}
	 */
	private static String extRegex;

	/**
	 * The default file description text to use. Default value is the empty String.
	 */
	private static String desc;

	/**
	 * Main driver.
	 * 
	 * @param args Program arguments.
	 */
	public static void main(String[] args)
	{
		CommandLine l = FCLI.gnuParse(makeOptList(), args, "SimpleUpload [-c] [-d] [-h] files");
		desc = l.getOptionValue('d', "");

		wiki = Toolbox.getFastilyClone();
		if (l.hasOption('c'))
			wiki = Toolbox.getCommons(wiki);

		extRegex = WikiX.allowedFileExtsRegex(wiki);

		Stream.of(args).filter(s -> s.matches(extRegex)).map(Paths::get).filter(Files::exists)
				.forEach(p -> wiki.upload(p, p.getFileName().toString(), desc, ""));
	}

	/**
	 * Makes the list of CLI options.
	 * 
	 * @return The list of CommandLine options.
	 */
	private static Options makeOptList()
	{
		Options ol = FCLI.makeDefaultOptions();
		ol.addOption(FCLI.makeArgOption("d", "Set the description text", "descText"));
		ol.addOption("c", "Upload to Commons instead of en.wikipedia.org");
		return ol;
	}
}