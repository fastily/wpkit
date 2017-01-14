package ctools.tplate;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import fastily.jwiki.util.GSONP;

/**
 * Utilities used by the <code>tplate</code> package.
 * 
 * @author Fastily
 *
 */
final class TUtils
{
	/**
	 * Constructors disallowed
	 */
	private TUtils()
	{

	}

	/**
	 * Creates a single Object ArrayList.
	 * 
	 * @param t The Object to create the ArrayList with.
	 * @return The single Object ArrayList
	 */
	private static <T> ArrayList<T> sgAL(T t)
	{
		return new ArrayList<>(Collections.singletonList(t));
	}

	/**
	 * Gets JSONObject for a key which could either be a JSONArray of JSONObject or just a single JSONObject.
	 * 
	 * @param r The Reply to use.
	 * @param key The key to look at.
	 * @return An ArrayList with any JSONObject found, or the empty ArrayList if nothing matching was found.
	 */
	protected static ArrayList<JsonObject> getJAOf(JsonObject r, String key)
	{
		if (r.has(key))
		{
			JsonElement x = r.get(key);

			if (x.isJsonObject())
				return sgAL(x.getAsJsonObject());
			else if (x.isJsonArray())
				return GSONP.getJAofJO(x.getAsJsonArray());
		}

		return new ArrayList<>();
	}

	/**
	 * Gets Strings for a key which could either be a single String or a JSONArray of Strings.
	 * 
	 * @param r The Reply to use.
	 * @param key The key to look at.
	 * @return An ArrayList with any Strings found, or the empty ArrayList if nothing matching was found.
	 */
	protected static ArrayList<String> strsFromJA(JsonObject r, String key)
	{
		if (r.has(key))
		{
			JsonElement x = r.get(key);
			if (x.isJsonArray())
				return GSONP.jaOfStrToAL(x.getAsJsonArray());
			else if (x.isJsonPrimitive())
			{
				JsonPrimitive p = x.getAsJsonPrimitive();
				return p.isString() ? sgAL(p.getAsString()) : sgAL(x.toString());
			}
		}

		return new ArrayList<>();
	}
}