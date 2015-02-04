package util;

import java.util.Arrays;
import java.util.List;

/**
 * Miscellaneous String related methods.
 * 
 * @author Fastily
 *
 */
public class StringTools
{
	/**
	 * Constructors disallowed
	 */
	private StringTools()
	{
		
	}
	
	/**
	 * Capitalizes the first character of a String
	 * 
	 * @param s The String to work with
	 * @return A new copy of the passed in String, with the first character capitalized.
	 */
	public static String capitalize(String s)
	{
		return s.length() < 2 ? s.toUpperCase() : s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	
	/**
	 * Determines if two String arrays share elements.
	 * 
	 * @param a Array 1
	 * @param b Array 2
	 * @return True if the arrays intersect.
	 */
	public static boolean arraysIntersect(String[] a, String[] b)
	{
		return arraysIntersect(Arrays.asList(a), Arrays.asList(b));
	}

	/**
	 * Determines if two String Lists share elements.
	 * 
	 * @param a List 1
	 * @param b List 2
	 * @return True if the Lists intersect.
	 */
	public static boolean arraysIntersect(List<String> a, List<String> b)
	{
		for (String s : a)
			if (b.contains(s))
				return true;
		return false;
	}
	

	/**
	 * Makes a regex for replacing titles/files on a page. Converts regex operators to their escaped counterparts.
	 * 
	 * @param title The title to convert into a regex.
	 * @return The regex.
	 */
	public static String makePageTitleRegex(String title)
	{
		String temp = new String(title);
		for (String s : new String[] { "(", ")", "[", "]", "{", "}", "^", "-", "=", "$", "!", "|", "?", "*", "+", ".", "<",
				">" })
			temp = temp.replace(s, "\\" + s);
		temp = temp.replaceAll("( |_)", "( |_)");
		return "(?si)(" + temp + ")";
	}

}