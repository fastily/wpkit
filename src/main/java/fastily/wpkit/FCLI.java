package fastily.wpkit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import fastily.jwiki.util.FSystem;
import fastily.jwiki.util.WGen;

/**
 * Static methods supporting CLI functionality.
 * 
 * @author Fastily
 * 
 */
public final class FCLI
{

	/**
	 * All static methods, no constructors allowed.
	 */
	private FCLI()
	{

	}

	/**
	 * Make an option which takes a single arg.
	 * 
	 * @param title The title of the option
	 * @param desc Description of this option for man page.
	 * @param argname The name of the argument for man page.
	 * @return The resulting option.
	 */
	public static Option makeArgOption(String title, String desc, String argname)
	{
		Option o = new Option(title, true, desc);
		o.setArgName(argname);
		return o;
	}

	/**
	 * Create a set of mutually exclusive options.
	 * 
	 * @param ol The list of options to make an option group with.
	 * @return The option group.
	 */
	public static OptionGroup makeOptGroup(Option... ol)
	{
		OptionGroup og = new OptionGroup();
		for (Option o : ol)
			og.addOption(o);

		return og;
	}

	/**
	 * Use the GNU parser to parse a list of args with the given option set. Auto-prints specified help message if
	 * {@code help} option is detected. Exits program if an error is detected.
	 * 
	 * @param ol The option group to use
	 * @param args The argument list to parse
	 * @param help The help message to print if {@code help} is requested.
	 * @return A CommandLine Object.
	 */
	public static CommandLine gnuParse(Options ol, String[] args, String help)
	{
		CommandLine l = null;

		try
		{
			l = new DefaultParser().parse(ol, args);
		}
		catch (Throwable e)
		{
			FSystem.errAndExit(e, null);
		}

		if (l.hasOption("help"))
		{
			HelpFormatter hf = new HelpFormatter();
			hf.setWidth(120);
			hf.printHelp(help, ol);
			System.exit(0);
		}
		if (l.hasOption("wgen"))
		{
			try
			{
				WGen.main(args);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
			System.exit(0);
		}

		return l;
	}

	/**
	 * Creates some default options for us.
	 * 
	 * @return The list of options we made.
	 */
	public static Options makeDefaultOptions()
	{
		Options ol = new Options();
		ol.addOption("help", "help", false, "Print this help message and exit");
		ol.addOption("wgen", "wgen", false, "Invoke the WikiGen utility and exit");
		return ol;
	}
}