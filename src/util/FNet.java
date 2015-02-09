package util;

import java.net.URL;

import jwiki.core.Req;
import jwiki.util.FString;

/**
 * Network related functions
 * 
 * @author Fastily
 *
 */
public class FNet
{
	/**
	 * Constructors disalllowed
	 */
	private FNet()
	{

	}

	/**
	 * Performs a very simple GET (no cookies) request on the given url.
	 * 
	 * @param u The URL to retrieve.
	 * @return The contents found at the url.
	 */
	public static String get(String u)
	{
		try
		{
			return FString.inputStreamToString(Req.genericGET(new URL(u), null));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
}