package commons;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import util.FIO;
import jwiki.core.CAction;
import jwiki.core.MBot;
import jwiki.core.MQuery;
import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.dwrap.Contrib;
import jwiki.dwrap.ImageInfo;
import jwiki.util.FL;
import jwiki.util.Tuple;

//TODO: This documentation has seen better days.

/**
 * A collection of static Commons-specific methods I find myself using a lot.
 * @author Fastily
 *
 */
public class Commons
{
	/**
	 * Delete titles in a category.
	 * 
	 * @param wiki The wiki object to use
	 * @param cat The category to delete items in
	 * @param reason The reason to use
	 * @param delCat Set to true if the category should be deleted after deleting everything in it. Category is only
	 *           deleted if it is empty.
	 * @param ns Namespace filter - select only items in these namespace(s).  Leave blank to select all namespaces.
	 * @return A list of titles we didn't delete.
	 */
	public static ArrayList<String> categoryNuke(Wiki wiki, String cat, String reason, boolean delCat, NS... ns)
	{
		ArrayList<String> fails = CAction.delete(wiki, reason, wiki.getCategoryMembers(cat, ns));
		if (delCat && wiki.getCategorySize(cat) == 0)
			wiki.delete(cat, CStrings.Reason.ec.rsn);
		return fails;
	}

	/**
	 * Delete all files linked to a DR. Sets deletion log reason to a link of to the DR.
	 * 
	 * @param dr The dr from which to get files.
	 * @return A list of pages we failed to delete.
	 */
	public static ArrayList<String> drDel(Wiki wiki, String dr)
	{
		return nukeLinksOnPage(wiki, dr, "[[" + dr + "]]", NS.FILE);
	}

	/**
	 * Restore pages from a list in a file.
	 * 
	 * @param reason The reason to use
	 * @param path The path to the file
	 * @return A list of pages we didn't restore.
	 */
	public static ArrayList<String> restoreFromFile(Wiki wiki, String path, String reason)
	{
		return CAction.undelete(wiki, true, reason, FIO.readLinesFromFile(path));
	}

	/**
	 * Nukes empty files or files without an associated description page.
	 * 
	 * @param files A list of pages in the file namespace. PRECONDITION -- The files must be in the filenamespace.
	 * @return A list of titles which couldn't be processed
	 */
	public static ArrayList<String> nukeEmptyFiles(Wiki wiki, ArrayList<String> files)
	{
		ArrayList<String> l = new ArrayList<>();
		for(Tuple<String, ImageInfo> ix : FL.mapToList(MQuery.getImageInfo(wiki, -1, -1, files)))
			if(ix.y.redirectsTo != null || ix.y.dimensions == null)
				l.add(ix.x);
		return CAction.delete(wiki, CStrings.Reason.nfu.rsn, l);
	}

	/**
	 * Checks if a category is empty and deletes it if true.
	 * 
	 * @param cats The categories to check and delete.
	 * @return A list of titles we failed to delete.
	 */
	public static ArrayList<String> emptyCatDel(Wiki wiki, ArrayList<String> cats)
	{
		ArrayList<String> l = new ArrayList<>();
		for (Tuple<String, Integer> t : FL.mapToList(MQuery.getCategorySize(wiki, cats)))
			if (t.y.intValue() <= 0) // Handle case when MediaWiki does not return categoryinfo
				l.add(t.x);
		return CAction.delete(wiki, CStrings.Reason.ec.rsn, l);
	}

	/**
	 * Delete the contributions of a user in the specified namespace.
	 * 
	 * @param user The user whose contribs we'll be deleting.
	 * @param reason Delete reason
	 * @param ns Namespace(s) of the items to delete.
	 * @return A list of titles we didn't delete.
	 */
	public static ArrayList<String> nukeContribs(Wiki wiki, String user, String reason, NS... ns)
	{
		ArrayList<String> l = new ArrayList<>();
		for (Contrib c : wiki.getContribs(user, ns))
			l.add(c.title);
		return CAction.delete(wiki, reason, l);
	}

	/**
	 * Delete uploads of a user.
	 * 
	 * @param user The user whose uploads we'll be deleting. 
	 * @param reason The reason to use
	 * @return A list of titles we didn't delete
	 */
	public static ArrayList<String> nukeUploads(Wiki wiki, String user, String reason)
	{
		return CAction.delete(wiki, reason, wiki.getUserUploads(user));
	}

	/**
	 * Deletes all links on a page in the specified namespace.
	 * 
	 * @param title The title to fetch links from
	 * @param reason Delete reason
	 * @param ns Only delete pages in this/these namespace(s). Optional param -- leave blank to select all namespaces.
	 * @return The links on the title in the requested namespace
	 * 
	 */
	public static ArrayList<String> nukeLinksOnPage(Wiki wiki, String title, String reason, NS... ns)
	{
		return CAction.delete(wiki, reason, wiki.getLinksOnPage(true, title, ns));
	}

	/**
	 * Deletes all images linked on a page.
	 * 
	 * @param title The title to fetch images from.
	 * @param reason The reason to use when deleting
	 * @return A list of files we failed to process.
	 */
	public static ArrayList<String> nukeImagesOnPage(Wiki wiki, String title, String reason)
	{
		return CAction.delete(wiki, reason, MQuery.exists(wiki, true, wiki.getImagesOnPage(title)));
	}

	/**
	 * Delete pages listed in a text file. Encoding should be UTF-8. One item per line.
	 * 
	 * @param path The path to the file to use.
	 * @param reason Reason to use when deleting
	 * @return A list of pages we failed to delete.
	 */
	public static ArrayList<String> nukeFromFile(Wiki wiki, String path, String reason)
	{
		return CAction.delete(wiki, reason, FIO.readLinesFromFile(path));
	}

	/**
	 * Removes <code>{{delete}}</code> templates from the listed titles.
	 * 
	 * @param reason Reason to use
	 * @param titles The titles to remove <code>{{delete}}</code> from
	 * @return A list of titles we didn't remove the templates from.
	 */
	public static ArrayList<String> removeDelete(Wiki wiki, String reason, ArrayList<String> titles)
	{
		return CAction.replace(wiki, CStrings.Regex.drregex.rgx, "", reason, titles);
	}

	/**
	 * Removes all no perm, lic, src templates from listed titles.
	 * 
	 * @param reason Reason to use
	 * @param titles The titles to remove no perm/lic/src templates from
	 * @return A list of titles we failed to remove the templates from.
	 */
	public static ArrayList<String> removeLSP(Wiki wiki, String reason, ArrayList<String> titles)
	{
		return CAction.replace(wiki, CStrings.Regex.delregex.rgx, "", reason, titles);
	}

	/**
	 * Replace all no perm, no lic, no src, copyvio, etc templates with a fresh (dated to today) npd template
	 * @param wiki The wiki to use
	 * @param titles The titles to process
	 * @return A list of titles we didn't process.
	 */
	public static ArrayList<String> resetNPD(Wiki wiki, ArrayList<String> titles)
	{
		return CAction.replace(wiki, CStrings.Regex.delregex.rgx, "{{Subst:Npd}}", "reset", titles);
	}
	
	/**
	 * Sends a file to DR and lists it on today's log.
	 * 
	 * @param reason The deletion reason to use
	 * @param files The files to send to DR.
	 * @return True if we were able to post to today's log.
	 */
	public static boolean sendToDR(Wiki wiki, String reason, String... files)
	{
		ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("UTC"));
		String header = "Commons:Deletion requests/";
		String listingfmt = "\n=== [[:%s]] ===\n%s ~~~~";

		int day = zdt.getDayOfMonth();
		int month = zdt.getMonthValue();
		int year = zdt.getYear();

		ArrayList<MBot.Task> wl = new ArrayList<>();
		for (String file : files)
			wl.add(new MBot.Task(file, null, null) {
				public boolean doJob(Wiki wiki)
				{
					return wiki.addText(title, String.format("{{delete|reason=%s|subpage=%s|day=%02d|month=%02d|year=%d}}\n",
							reason, file, day, month, year), "+dr", true)
							&& wiki.addText(header + title, String.format(listingfmt, title, reason), "start", false);
				}
			});

		wiki.submit(wl, 2);

		String x = "";
		for (String s : files)
			x += String.format("\n{{%s%s}}", header, s);

		return wiki.addText(String.format("%s%d/%02d/%02d", header, year, month, day), x, "+", false);
	}
}