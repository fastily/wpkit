package fastily.wpkit.tp;

import java.util.ArrayDeque;
import java.util.ArrayList;

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
	 * Recursively finds WTemplate objects contained by this WikiText.
	 * 
	 * @return A List of all WTemplate objects in this WikiText.
	 */
	public ArrayList<WTemplate> getTemplatesR()
	{
		ArrayList<WTemplate> wtl = new ArrayList<>();
		getTemplatesR(wtl);

		return wtl;
	}

	/**
	 * Recursively finds WTemplate objects contained by this WikiText.
	 * 
	 * @param wtl Any WTemplate objects found will be added to this List.
	 * 
	 * @see #getTemplatesR()
	 */
	private void getTemplatesR(ArrayList<WTemplate> wtl)
	{
		l.stream().filter(o -> o instanceof WTemplate).map(o -> (WTemplate) o).forEach(t -> {
			for (WikiText wt : t.params.values())
				wt.getTemplatesR(wtl);

			wtl.add(t);
		});
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