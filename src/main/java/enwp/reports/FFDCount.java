package enwp.reports;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import util.Toolbox;

/**
 * Keeps track of daily FfD count in a graph on wiki.
 * 
 * @author Fastily
 *
 */
public class FFDCount
{
	/**
	 * Main driver
	 * 
	 * @param args Program args
	 * @throws Throwable On i/o error
	 */
	public static void main(String[] args) throws Throwable
	{
		Wiki wiki = Toolbox.getFastilyBot();
		Path graphData = Paths.get("ffd-count.txt");
		ArrayList<String> xl = new ArrayList<>(), yl = new ArrayList<>();

		// Read in data
		if (Files.exists(graphData))
		{
			List<String> l = Files.readAllLines(graphData);
			if (l.size() >= 2)
			{
				getGraphData(xl, l.get(0));
				getGraphData(yl, l.get(1));
			}
		}

		// Calculate today's FfD total
		int total = 0;
		Matcher m = Pattern.compile("(?m)^\\* \\[\\[Wikipedia.+?\\d+? remaining$")
				.matcher(wiki.getPageText("Wikipedia:Files for discussion"));
		while (m.find())
		{
			String t = m.group();
			total += Integer.parseInt(t.substring(t.indexOf('-') + 1, t.indexOf("remaining")).trim());
		}

		yl.add("" + total);

		// Get and use today's date
		LocalDate ld = LocalDate.now();
		xl.add(String.format("%d/%d", ld.getMonthValue(), ld.getDayOfMonth()));

		// Truncate to most recent 30 results
		truncateAL(xl, 30);
		truncateAL(yl, 30);

		// Edit
		String x = String.join(",", xl), y = String.join(",", yl);
		wiki.edit(String.format("User:%s/FFDCount", wiki.whoami()),
				String.format(
						"{{Graph:Chart|width=1200|height=400|type=line|xAxisTitle=Date|yAxisTitle=Open FfDs|x=%s|y=%s}}%nUpdated ~~~~~", x,
						y),
				"Updating report");

		// Save results to disk for next run
		Files.write(graphData, FL.toSAL(x, y), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING);
	}

	/**
	 * Truncates an ArrayList in size if it is larger than a specified amount.
	 * 
	 * @param l The ArrayList to truncate
	 * @param max The maximum size this <code>l</code> should be
	 */
	private static void truncateAL(ArrayList<String> l, int max)
	{
		if (l.size() > max)
			l.subList(0, max);
	}

	/**
	 * Parses an axis of raw graph data
	 * 
	 * @param l The ArrayList to load parsed values into
	 * @param raw The raw, unparsed axis retrieved from the graph data
	 */
	private static void getGraphData(ArrayList<String> l, String raw)
	{
		l.addAll(Arrays.asList(raw.split(",")));
	}
}