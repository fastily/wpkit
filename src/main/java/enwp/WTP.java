package enwp;

import java.util.ArrayList;
import java.util.HashSet;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwikix.core.TParse;

/**
 * Lists commonly used templates and contains methods to get regexes and transclusions for them.
 * 
 * @author Fastily
 *
 */
public final class WTP
{
	/**
	 * Wraps <code>{{Copy to Wikimedia Commons}}</code>
	 */
	public static final WTP mtc = new WTP("Template:Copy to Wikimedia Commons");

	/**
	 * Wraps <code>{{Bots}}</code>
	 */
	public static final WTP nobots = new WTP("Template:Bots");

	/**
	 * Wraps <code>{{Now Commons}}</code>
	 */
	public static final WTP ncd = new WTP("Template:Now Commons");

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