package enwp;

import java.util.ArrayList;
import java.util.HashSet;

import ctools.util.TParse;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;

/**
 * Lists commonly used templates and contains methods to get regexes and transclusions for them.
 * 
 * @author Fastily
 *
 */
public final class WTP
{
	/**
	 * Wraps {@code Template:Copy to Wikimedia Commons}
	 */
	public static final WTP mtc = new WTP("Template:Copy to Wikimedia Commons");

	/**
	 * Wraps {@code Template:Nominated for deletion on Commons}
	 */
	public static final WTP nomDelOnCom = new WTP("Template:Nominated for deletion on Commons");

	/**
	 * Wraps {@code Template:Bots}
	 */
	public static final WTP nobots = new WTP("Template:Bots");

	/**
	 * Wraps {@code Template:Now Commons}
	 */
	public static final WTP ncd = new WTP("Template:Now Commons");

	/**
	 * Wraps {@code Template:Orphan image}
	 */
	public static final WTP orphan = new WTP("Template:Orphan image");

	/**
	 * Wraps {@code Template:Keep local}
	 */
	public static final WTP keeplocal = new WTP("Template:Keep local");

	/**
	 * The template title, including namespace
	 */
	public final String title;

	/**
	 * The String regex of the template.
	 */
	private String rgx;

	/**
	 * Constructor, creates a WTP
	 * 
	 * @param title The title, including namespace.
	 */
	public WTP(String title)
	{
		this.title = title;
	}

	/**
	 * Gets a regex for this WTP. This method is cached.
	 * 
	 * @param wiki Uses the specified Wiki to generate regexes, if applicable.
	 * @return A regex matching the template wrapped by this WTP.
	 */
	public String getRegex(Wiki wiki)
	{
		return rgx != null ? rgx : (rgx = TParse.makeTemplateRegex(wiki, title));
	}

	/**
	 * Gets the transclusions of this template as an ArrayList.
	 * 
	 * @param wiki The Wiki object to use
	 * @param ns Only pages in these Namespaces will be returned. Optional param - leave blank to disable.
	 * @return An ArrayList with transclusions of the template.
	 */
	public ArrayList<String> getTransclusionList(Wiki wiki, NS... ns)
	{
		return wiki.whatTranscludesHere(title, ns);
	}

	/**
	 * Gets the transclusions of this template as an HashSet.
	 * 
	 * @param wiki The Wiki object to use
	 * @param ns Only pages in these Namespaces will be returned. Optional param - leave blank to disable.
	 * @return A HashSet with transclusions of the template.
	 */
	public HashSet<String> getTransclusionSet(Wiki wiki, NS... ns)
	{
		return new HashSet<>(getTransclusionList(wiki, ns));
	}
}