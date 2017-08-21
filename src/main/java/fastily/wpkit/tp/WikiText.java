package fastily.wpkit.tp;

import java.util.ArrayDeque;

/**
 * Represents wikitext. May contain Strings and templates.
 * 
 * @author Fastily
 *
 */
public class WikiText
{
	/**
	 * Data structure backing wikitext storage.
	 */
	protected ArrayDeque<Object> l = new ArrayDeque<>();

	/**
	 * Creates a new WikiText object
	 * 
	 * @param objects Any Objects to pre-load this WikiText object with. Acceptable values are of type String or
	 *           WTemplate.
	 */
	public WikiText(Object... objects)
	{
		for (Object o : objects)
			append(o);
	}

	/**
	 * Appends an Object to this WikiText object.
	 * 
	 * @param o The Object to append. Acceptable values are of type String or WTemplate.
	 */
	public void append(Object o)
	{
		if (o instanceof String)
			l.add((l.peekLast() instanceof String ? l.pollLast().toString() : "") + o);
		else if (o instanceof WTemplate)
		{
			WTemplate t = (WTemplate) o;
			t.parent = this;
			l.add(o);
		}
		else
			throw new IllegalArgumentException("What is '" + o + "' ?");
	}
	
	/**
	 * Render this WikiText object as a String.
	 */
	public String toString()
	{
		String x = "";
		for (Object o : l)
			x += o;
		return x;
	}
}