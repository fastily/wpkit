package ctools.tplate;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import fastily.jwiki.util.FL;
import fastily.jwiki.util.GSONP;

/**
 * Represents a Template parsed from MediaWiki's <code>parse</code> module.
 * 
 * @author Fastily
 *
 */
public class Template
{
	/**
	 * The title of the Template. This is generally the Template title without the <code>Template:</code> namespace
	 * prefix.
	 */
	public String title;

	/**
	 * The ParsedItem which owns this Template, if applicable.
	 */
	private ParsedItem parent = null;

	/**
	 * This Template's parameters.
	 */
	protected final TreeMap<String, TValue> params = new TreeMap<>(new TValueCmp());

	/**
	 * Creates a new Template Object with the given Reply.
	 * 
	 * @param r The Reply to create the Template with. PRECONDITION: This is a JSONObject that contains a Template
	 *           Object.
	 * @param parent The parent ParsedItem, if applicable. Optional param - set null to disable.
	 */
	protected Template(JsonObject r, ParsedItem parent)
	{
		this.parent = parent;
		title = GSONP.gString(r, "title");
		TUtils.getJAOf(r, "part").stream().forEach(p -> params.put(resolveName(p), new TValue(p.get("value"))));
	}

	/**
	 * Creates a new Template
	 * 
	 * @param title The title of the Template. This should not include the <code>Template:</code> prefix.
	 * @param params Optional initial parameters to give the Template, of the form
	 *           <code>[key1, value1, key2, value2, ...]</code>. Each value in the pair will be interpreted as a String.
	 */
	public Template(String title, String... params)
	{
		this.title = title;
		put(FL.pMap(params));
	}

	/**
	 * Puts a Map of key-value parameters in this Template.
	 * 
	 * @param m The Map to use. The values in the Map must be either String or ParsedItem.
	 */
	public void put(Map<String, ?> m)
	{
		for (Map.Entry<String, ?> e : m.entrySet())
			put(e.getKey(), e.getValue());
	}

	/**
	 * Puts a key value parameter in this Template.
	 * 
	 * @param k The key to use
	 * @param v The value, which must be a String or ParsedItem.
	 */
	public void put(String k, Object v)
	{
		params.put(k, new TValue(v));
	}

	/**
	 * Append a String value to a Template parameter. Creates a new parameter for the Template if <code>k</code> does not
	 * have a corresponding value.
	 * 
	 * @param k The key to use
	 * @param s The String to append.
	 */
	public void append(String k, String s)
	{
		if (!has(k))
			put(k, s);
		else
		{
			TValue v = params.get(k);
			if (v.isString())
				v.setValue(v.getString() + s);
			else
				v.getParsedItem().contents.add(s);
		}
	}

	/**
	 * Gets the value associated with a specific key.
	 * 
	 * @param key The key to get a TValue for.
	 * @return The TValue, or null if a TValue doesn't exist for the specified key.
	 */
	public TValue get(String key)
	{
		return params.get(key);
	}

	/**
	 * Determines if a Template has a given key.
	 * 
	 * @param k The key to search for
	 * @return True if the Template contains the specified key.
	 */
	public boolean has(String k)
	{
		return params.containsKey(k);
	}

	/**
	 * Drops this Template from any parent ParsedItem which contains this Template in its <code>tplates</code> list.
	 * 
	 * @return This Object, for chaining convenience.
	 */
	public Template drop()
	{
		if (parent != null)
			parent.tplates.remove(this);
		return this;
	}

	/**
	 * Resolves the name of a template parameter from raw JSON.
	 * 
	 * @param jo The JSONObject with template parameter data. PRECONDITION: this must contain a 'name' field.
	 * @return A String with the name of the template parameter, or null if nothing matching was found.
	 */
	private static String resolveName(JsonObject jo)
	{
		JsonElement o = jo.get("name");

		if (o.isJsonObject())
			return "" + o.getAsJsonObject().get("index").getAsInt();
		else if (o.isJsonPrimitive())
		{
			JsonPrimitive p = o.getAsJsonPrimitive();
			return p.isString() ? p.getAsString() : o.toString();
		}

		return null;
	}

	/**
	 * Generates a wikitext representation of this Template.
	 */
	public String toString()
	{
		return toString(false);
	}

	/**
	 * Generates a wikitext representation of this Template.
	 * 
	 * @param indent Set True to add a newline between each parameter line.
	 * @return A wikitext representation of this Template.
	 */
	public String toString(boolean indent)
	{
		String base = (indent ? "%n" : "") + "|%s=%s";

		String x = "";
		for (Map.Entry<String, TValue> e : params.entrySet())
			x += String.format(base, e.getKey(), e.getValue());

		if (indent)
			x += "\n";

		return String.format("{{%s%s}}", title, x);
	}

	/**
	 * Represents a Template parameter value.
	 * 
	 * @author Fastily
	 *
	 */
	public static class TValue
	{
		/**
		 * The variable storing a String value if this TValue contains a String.
		 */
		private String sVal;

		/**
		 * The variable storing a ParsedItem value if this TValue contains a ParsedItem.
		 */
		private ParsedItem pVal;

		/**
		 * Constructor, creates a TValue
		 * 
		 * @param o A String or ParsedItem to be stored in the TValue. PRECONDITION: <code>o</code> is either a String or
		 *           ParsedItem.
		 */
		protected TValue(Object o)
		{
			setValue(o);
		}

		/**
		 * Sets the value of this TValue with the given Object.
		 * 
		 * @param o A String or ParsedItem to set this TValue to. PRECONDITION: This must be either a String or
		 *           ParsedItem.
		 */
		public void setValue(Object o)
		{
			sVal = null;
			pVal = null;

			if (o instanceof JsonObject)
				pVal = new ParsedItem((JsonObject) o);
			else if(o instanceof JsonPrimitive)
			{
				JsonPrimitive p = (JsonPrimitive) o;
				sVal = p.isString() ? p.getAsString() : p.toString();
			}
			else
				sVal = o.toString();
		}

		/**
		 * Determines whether this TValue contains a String.
		 * 
		 * @return True if the TValue contains a String.
		 */
		public boolean isString()
		{
			return sVal != null;
		}

		/**
		 * Determines whether this TValue contains a ParsedItem.
		 * 
		 * @return True if the TValue contains a ParsedItem.
		 */
		public boolean isParsedItem()
		{
			return pVal != null;
		}

		/**
		 * Gets the String wrapped by this TValue, if possible.
		 * 
		 * @return The String wrapped by this TValue, or null if it does not wrap a String value.
		 */
		public String getString()
		{
			return sVal;
		}

		/**
		 * Gets the ParsedItem wrapped by this TValue, if possible.
		 * 
		 * @return The ParsedItem wrapped by this TValue, or null if it does not wrap a ParsedItem value.
		 */
		public ParsedItem getParsedItem()
		{
			return pVal;
		}

		/**
		 * Creates a wikitext representation of this template value.
		 */
		public String toString()
		{
			return sVal == null ? pVal.toString() : sVal;
		}
	}

	/**
	 * A Comparator for Template titles or Template <code>params</code> keys.
	 * 
	 * @author Fastily
	 *
	 */
	public static class TValueCmp implements Comparator<String>
	{
		/**
		 * Does a compareIgnoreCase, but also a compareIgnoreCase where underscores for both inputs are substituted for
		 * spaces.
		 */
		public int compare(String o1, String o2)
		{
			int r1 = o1.compareToIgnoreCase(o2);
			return r1 == 0 ? r1 : o1.replace('_', ' ').compareToIgnoreCase(o2.replace('_', ' '));
		}
	}
}