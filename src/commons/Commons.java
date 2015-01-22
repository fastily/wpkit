package commons;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import jwiki.core.CAction;
import jwiki.core.Contrib;
import jwiki.core.MBot;
import jwiki.core.MQuery;
import jwiki.core.Namespace;
import jwiki.core.Wiki;
import jwiki.util.ReadFile;
import jwiki.util.Tuple;

/**
 * Special multi-threaded commons.wikimedia.org exclusive methods which make life so much easier.
 * 
 * @author Fastily
 */
public class Commons
{
	/**
	 * The admin object to use.
	 */
	private Wiki admin = null;

	/**
	 * The normal user object to use.
	 */
	private Wiki clone;

	/**
	 * Creates a Commons object for us. Non-admin tasks will be assigned to <tt>wiki</tt>, whereas admin tasks will be
	 * run from <tt>admin</tt>. Current domain of either object does not have to be set to Commons.
	 * 
	 * @param wiki wiki object to use for non-admin tasks. PRECONDITION: this field cannot be null.
	 * @param admin wiki object for admin tasks. If you're not an admin, you ought to specify this to be null, otherwise
	 *           you may get strange behavior if you execute admin tasks.
	 */
	public Commons(Wiki wiki, Wiki admin)
	{
		clone = wiki;
		if (admin != null && !admin.listGroupsRights(admin.whoami()).contains("sysop"))
			throw new IllegalArgumentException(String.format("%s is not an admin but was claimed to be one!", admin.whoami()));

		this.admin = admin;
	}

	/**
	 * Deletes everything in Category:Fastily Test as uploader requested.
	 * 
	 * @param exit Set to true if program should exit after procedure completes.
	 * @return A list of titles which couldn't be deleted.
	 */
	public ArrayList<String> nukeFastilyTest(boolean exit)
	{
		ArrayList<String> fails = categoryNuke("Fastily Test", CStrings.ur, false);
		if (exit)
			System.exit(0);
		return fails;
	}

	/**
	 * Deletes all the files in <a href="http://commons.wikimedia.org/wiki/Category:Copyright_violations"
	 * >Category:Copyright violations</a>.
	 */
	public ArrayList<String> clearCopyVios()
	{
		return categoryNuke(CStrings.cv, CStrings.copyvio, false, "File");
	}

	/**
	 * Deletes everything in <a href= "http://commons.wikimedia.org/wiki/Category:Other_speedy_deletions" >Category:Other
	 * speedy deletions</a>
	 * 
	 * @param reason Delete reason
	 * @param ns Namespace(s) to restrict deletion to. Leave blank to ignore namespace.
	 * @return A list of titles we didn't delete.
	 */
	public ArrayList<String> clearOSD(String reason, String... ns)
	{
		return categoryNuke(CStrings.osd, reason, false, ns);
	}

	/**
	 * Deletes the titles in a category.
	 * 
	 * @param cat The category to nuke items from
	 * @param reason Delete reason
	 * @param delCat Set to true if the category should be deleted after deleting everything in it. Category is only
	 *           deleted if it is empty.
	 * @param ns Namespace filter -- anything in these namespace(s) will be deleted. Optional param -- leave blank to
	 *           select all namesapces
	 * @return A list of titles we didn't delete.
	 */
	public ArrayList<String> categoryNuke(String cat, String reason, boolean delCat, String... ns)
	{
		//TODO: getCategorySize breaks if namespace not supplied
		ArrayList<String> fails = CAction.delete(admin, reason, admin.getCategoryMembers(cat, ns));
		if (delCat && admin.getCategorySize(cat) == 0)
			admin.delete(cat, CStrings.ec);
		return fails;
	}

	/**
	 * Delete all files linked to a DR. Sets deletion log reason to a link of to the DR.
	 * 
	 * @param dr The dr from which to get files.
	 * @return A list of pages we failed to delete.
	 */
	public ArrayList<String> drDel(String dr)
	{
		return nukeLinksOnPage(dr, "[[" + dr + "]]", "File");
	}

	/**
	 * Restore pages from a list in a file.
	 * 
	 * @param reason The reason to use
	 * @param path The path to the file
	 * @return A list of pages we didn't restore.
	 */
	public ArrayList<String> restoreFromFile(String path, String reason)
	{
		return CAction.undelete(admin, true, reason, new ReadFile(path).l);
	}

	// TODO: This likely won't be necessary once fileinfo is released. Disabled for now.
	/**
	 * Nukes empty files or files without an associated description page.
	 * 
	 * @param files A list of pages in the file namespace. PRECONDITION -- The files must be in the filenamespace.
	 * @return A list of titles which couldn't be processed
	 */
	public ArrayList<String> nukeEmptyFiles(ArrayList<String> files)
	{
		/*
		 * ArrayList<WAction> l = new ArrayList<>(); for (String s : files) l.add(new WAction(s, null, CStrings.nfu) {
		 * public boolean doJob(Wiki wiki) { return wiki.getImageInfo(title) == null ? wiki.delete(title, summary) : true;
		 * } });
		 * 
		 * return WAction.toString(admin.mbot.start(l));
		 */
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks if a category is empty and deletes it if true.
	 * 
	 * @param cats The categories to check and delete.
	 * @return A list of titles we failed to delete.
	 */
	public ArrayList<String> emptyCatDel(ArrayList<String> cats)
	{
		ArrayList<String> l = new ArrayList<>();
		for (Tuple<String, Integer> t : MQuery.getCategorySize(admin, cats))
			if (t.y.intValue() == 0)
				l.add(t.x);
		return CAction.delete(admin, CStrings.ec, l);
	}

	/**
	 * Delete the contributions of a user in the specified namespace.
	 * 
	 * @param user The user whose contribs we'll be deleting.
	 * @param reason Delete reason
	 * @param ns Namespace(s) of the items to delete.
	 * @return A list of titles we didn't delete.
	 */
	public ArrayList<String> nukeContribs(String user, String reason, String... ns)
	{
		ArrayList<String> l = new ArrayList<>();
		for (Contrib c : admin.getContribs(user, ns))
			l.add(c.title);
		return CAction.delete(admin, reason, l);
	}

	/**
	 * Delete uploads of a user.
	 * 
	 * @param user The user whose uploads we'll be deleting.
	 * @param reason The reason to use
	 * @return A list of titles we didn't delete
	 */
	public ArrayList<String> nukeUploads(String user, String reason)
	{
		return CAction.delete(admin, reason, admin.getUserUploads(Namespace.nss(user)));
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
	public ArrayList<String> nukeLinksOnPage(String title, String reason, String... ns)
	{
		return CAction.delete(admin, reason, admin.getLinksOnPage(true, title, ns));
	}

	/**
	 * Deletes all images linked on a page.
	 * 
	 * @param title The title to fetch images from.
	 * @param reason The reason to use when deleting
	 * @return A list of files we failed to process.
	 */
	public ArrayList<String> nukeImagesOnPage(String title, String reason)
	{
		return CAction.delete(admin, reason, MQuery.exists(admin, true, admin.getImagesOnPage(title)));
	}

	/**
	 * Delete pages listed in a text file. Encoding should be UTF-8. One item per line.
	 * 
	 * @param path The path to the file to use.
	 * @param reason Reason to use when deleting
	 * @return A list of pages we failed to delete.
	 */
	public ArrayList<String> nukeFromFile(String path, String reason)
	{
		return CAction.delete(admin, reason, new ReadFile(path).l);
	}

	/**
	 * Removes <code>{{delete}}</code> templates from the listed titles.
	 * 
	 * @param reason Reason to use
	 * @param titles The titles to remove <code>{{delete}}</code> from
	 * @return A list of titles we didn't remove the templates from.
	 */
	public ArrayList<String> removeDelete(String reason, ArrayList<String> titles)
	{
		return CAction.replace(clone, CStrings.drregex, "", reason, titles);
	}

	/**
	 * Removes all no perm, lic, src templates from listed titles.
	 * 
	 * @param reason Reason to use
	 * @param titles The titles to remove no perm/lic/src templates from
	 * @return A list of titles we failed to remove the templates from.
	 */
	public ArrayList<String> removeLSP(String reason, ArrayList<String> titles)
	{
		return CAction.replace(clone, CStrings.delregex, "", reason, titles);
	}

	/**
	 * Sends a file to DR and lists it on today's log.
	 * 
	 * @param reason The deletion reason to use
	 * @param files The files to send to DR.
	 * @return True if we were able to post to today's log.
	 */
	public boolean sendToDR(String reason, String... files)
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

		clone.submit(wl, 2);

		String x = "";
		for (String s : files)
			x += String.format("\n{{%s%s}}", header, s);

		return clone.addText(String.format("%s%d/%02d/%02d", header, year, month, day), x, "+", false);
	}
}