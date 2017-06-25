package fastily.wpkit.tplate;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.json.XML;

import com.google.gson.JsonObject;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.GSONP;

/**
 * Represents a segment of parsed wikitext.
 * 
 * @author Fastily
 *
 */
public class ParsedItem
{
	/**
	 * List with parsed comments or miscellaneous page contents
	 */
	public final ArrayList<String> comments, contents;

	/**
	 * Templates contained by this object.
	 */
	public final ArrayList<Template> tplates;

	/**
	 * Creates a ParsedItem with the given Reply.
	 * 
	 * @param r The Reply to use. PRECONDITION: This is a <code>root</code> JSONObject for the ParsedItem.
	 */
	protected ParsedItem(JsonObject r)
	{
		comments = TUtils.strsFromJA(r, "comment");
		contents = TUtils.strsFromJA(r, "content");
		tplates = FL.toAL(TUtils.getJAOf(r, "template").stream().map(x -> new Template(x, this)));
	}

	/**
	 * Parses specified wikitext and creates a ParsedItem.
	 * 
	 * @param wiki The Wiki object to make the API call with
	 * @param title The title to use as if the specified wikitext were on this page. Optional param - set null to
	 *           disable.
	 * @param text The wikitext to parse
	 * @return A ParsedItem representation of this wikitext.
	 */
	public static ParsedItem parse(Wiki wiki, String title, String text)
	{
		ArrayList<String> l = FL.toSAL("text", text);
		if (title != null)
		{
			l.add("title");
			l.add(title);
		}

		return parse(wiki, l);
	}

	/**
	 * Parses the content of a Wiki page and creates a ParsedItem.
	 * 
	 * @param wiki The Wiki to make the API call with
	 * @param page The title of the page to parse
	 * @return A ParsedItem representation of <code>page</code>
	 */
	public static ParsedItem parse(Wiki wiki, String page)
	{
		return parse(wiki, FL.toSAL("page", page));
	}

	/**
	 * Performs <code>action=parse</code> on a page with a given set of parameters.
	 * 
	 * @param wiki The Wiki object to use
	 * @param params The control parameters to apply. Ex: <code>page</code>, <code>text</code>, <code>title</code>.
	 * @return A ParsedItem representation of the page/text.
	 */
	private static ParsedItem parse(Wiki wiki, ArrayList<String> params)
	{
		ArrayList<String> pl = FL.toSAL("prop", "parsetree");
		pl.addAll(params);
		try
		{
			return new ParsedItem(GSONP.jp.parse(XML.toJSONObject(GSONP.gString(
					GSONP.getNestedJO(GSONP.jp.parse(wiki.basicGET("parse", pl.toArray(new String[0])).body().string()).getAsJsonObject(),
							FL.toSAL("parse", "parsetree")),
					"*")).toString()).getAsJsonObject().getAsJsonObject("root"));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Recursively gets nested ParsedItem objects.
	 * 
	 * @return A Stream containing any found nested ParsedItem objects.
	 */
	private Stream<ParsedItem> getNestedParsedItems()
	{
		return tplates.stream().flatMap(t -> FL.mapToList(t.params).stream()).filter(e -> e.y.isParsedItem())
				.map(e -> e.y.getParsedItem());
	}

	/**
	 * Recursively finds Template objects contained by any nested ParsedItem objects. This is a helper method for
	 * <code>getTemplateR()</code>
	 * 
	 * @param tl Templates found will be added to this ArrayList.
	 * 
	 * @see #getTemplateR()
	 */
	private void getTemplatesR(ArrayList<Template> tl)
	{
		tl.addAll(tplates);
		getNestedParsedItems().forEach(pi -> pi.getTemplatesR(tl));
	}

	/**
	 * Recursively gets Template objects contained by any nested ParsedItem objects.
	 * 
	 * @return An ArrayList with any nested Template objects.
	 */
	public ArrayList<Template> getTemplateR()
	{
		ArrayList<Template> tl = new ArrayList<>();
		getTemplatesR(tl);

		return tl;
	}

	/**
	 * Recursively gets the content (ie anything which is not a template/comment/header) of this ParsedItem. This is a
	 * helper method for <code>getContentsR()</code>
	 * 
	 * @param cl The ArrayList where the content of nested ParsedItem objects will be copied to.
	 * 
	 * @see #getContentsR()
	 */
	private void getContentsR(ArrayList<String> cl)
	{
		cl.addAll(contents);
		getNestedParsedItems().forEach(pi -> pi.getContentsR(cl));
	}

	/**
	 * Recursively gets the content (ie anything which is not a template/comment/header) of this ParsedItem.
	 * 
	 * @return A String containing any content.
	 */
	public String getContentsR()
	{
		ArrayList<String> cl = new ArrayList<>();
		getContentsR(cl);

		return String.join(" ", cl.toArray(new String[0]));
	}

	/**
	 * Renders this ParsedItem as wikitext. Does not pretty-print output.
	 */
	public String toString()
	{
		String x = "";
		for (Template t : tplates)
			x += t.toString();

		x += String.join(" ", comments.toArray(new String[0]));
		x += String.join(" ", contents.toArray(new String[0]));

		return x;
	}
}