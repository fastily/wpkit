package fastily.wpkit.tp;

import java.util.Map;
import java.util.TreeMap;

import fastily.wpkit.util.WikiX;

/**
 * Represents a parsed, wiki-text template.
 * 
 * @author Fastily
 *
 */
public class WTemplate
{
	/**
	 * The parent WikiText object, if necessary
	 */
	protected WikiText parent;

	/**
	 * This WTemplate's title
	 */
	protected String title = "";

	/**
	 * The Map tracking this object's parameters.
	 */
	protected final TreeMap<String, WikiText> params = new TreeMap<>(WikiX.tpParamCmp);

	/**
	 * Creates a new, empty WTemplate object.
	 */
	public WTemplate()
	{
		this(null);
	}

	/**
	 * Creates a new WTemplate with a parent.
	 * 
	 * @param parent The parent WikiText object this WTemplate belongs to.
	 */
	protected WTemplate(WikiText parent)
	{
		this.parent = parent;
	}

	/**
	 * Puts a new parameter in this Template.
	 * 
	 * @param k The name of the parameter
	 * @param v The value of the parameter; acceptable types are WikiText, String, and WTemplate.
	 */
	public void put(String k, Object v)
	{
		if (v instanceof WikiText)
			params.put(k, (WikiText) v);
		else if (v instanceof String || v instanceof WTemplate)
			params.put(k, new WikiText(v));
		else
			throw new IllegalArgumentException(String.format("'%s' is not an acceptable type", v));
	}

	/**
	 * Removes the mapping for the specified key, {@code k} 
	 * @param k Removes the mapping for this key, if possible
	 */
	public void removeParam(String k)
	{
		params.remove(k);
	}
	
	public void append(String key, Object value) // TODO: Fixme - typing; do I even need this?
	{
		if (params.containsKey(key))
			params.get(key).append(value);
		else
			put(key, value);
	}

	/**
	 * Removes this WTemplate from its parent WikiText object, if applicable.
	 */
	public void drop()
	{
		if(parent == null)
			return;
		
		parent.l.remove(this);
		parent = null;
	}
	
	/**
	 * Generates a String (wikitext) representation of this Template.
	 * 
	 * @param indent Set true to add a newline between each parameter.
	 * @return A String representation of this Template.
	 */
	public String toString(boolean indent)
	{
		String base = (indent ? "%n" : "") + "|%s=%s";

		String x = "";
		for (Map.Entry<String, WikiText> e : params.entrySet())
			x += String.format(base, e.getKey(), e.getValue());

		if (indent)
			x += "\n";

		return String.format("{{%s%s}}", title, x);
	}

	/**
	 * Renders this WTemplate as a String.
	 */
	public String toString()
	{
		return toString(false);
	}
}