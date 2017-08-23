package fastily.wpkit.tp;

import java.util.LinkedHashMap;
import java.util.Map;

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
	public String title = "";

	/**
	 * The Map tracking this object's parameters.
	 */
	protected LinkedHashMap<String, WikiText> params = new LinkedHashMap<>();

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
	 * Test if the specified key {@code k} exists in this WTemplate.
	 * @param k The key to check
	 * @return True if there is a mapping for {@code k} in this WTemplate.
	 */
	public boolean has(String k)
	{
		return params.containsKey(k);
	}
	
	/**
	 * Gets the specified WikiText value associated with key {@code k} in this WTemplate.
	 * @param k The key to get WikiText for.
	 * @return The WikiText, or null if there is no mappnig for {@code k}
	 */
	public WikiText get(String k)
	{
		return params.get(k);
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