package enwp.bots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import jwiki.core.MQuery;
import jwiki.core.NS;
import jwiki.core.TPlate;
import jwiki.core.Wiki;
import jwiki.util.FL;
import jwiki.util.Tuple;
import jwikix.core.TParse;
import util.Toolbox;

/**
 * Finds local enwp files transferred to Commons which have then been deleted on Commons.
 * 
 * @author Fastily
 *
 */
public final class FindDelComFFD
{
	/**
	 * The Wiki objects to use
	 */
	private static final Wiki enwp = Toolbox.getFastilyBot(), com = enwp.getWiki("commons.wikimedia.org");

	/**
	 * Nominated for deletion on Commons Template title
	 */
	private static final String nomDelTempl = "Template:Nominated for deletion on Commons";

	/**
	 * Nominated for deletion on Commons template-matching String regex
	 */
	private static final String nomDelTemplRegex = TParse.makeTemplateRegex(enwp, nomDelTempl);

	/**
	 * A Pattern representation of <code>nomDelTemplRegex</code>
	 */
	private static final Pattern nomDelTemplPattern = Pattern.compile(TParse.makeTemplateRegex(enwp, nomDelTempl));

	/**
	 * The Map of file names and page texts on enwp to work with.
	 */
	private static final HashMap<String, String> pageTexts = MQuery.getPageText(enwp, enwp.whatTranscludesHere(nomDelTempl, NS.FILE));

	/**
	 * Template String for template to be applied to files which have been deleted on Commons
	 */
	private static final String delOnCom = "{{Deleted on Commons|%s}}";

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used.
	 */
	public static void main(String[] args)
	{
		HashMap<String, String> comPairs = new HashMap<>();

		String comFile;
		for (Map.Entry<String, String> e : pageTexts.entrySet())
		{
			comFile = TPlate.parse(enwp, TParse.extractTemplate(nomDelTemplPattern, e.getValue())).getStringFor("1");
			if(comFile == null)
				comFile = e.getKey();
			
			comPairs.put(e.getKey(), enwp.convertIfNotInNS(comFile, NS.FILE));
		}

		HashMap<String, String> comDeletedPairs = new HashMap<>();
		for (String s : MQuery.exists(com, false, new ArrayList<>(comPairs.keySet())))
			if (!com.getLogs(comPairs.get(s), null, "delete", 1).isEmpty())
				comDeletedPairs.put(s, comPairs.get(s));
		
		for (Tuple<String, String> t : FL.mapToList(comDeletedPairs))
			enwp.edit(t.x, pageTexts.get(t.x).replaceAll(nomDelTemplRegex, String.format(delOnCom, enwp.nss(t.y))),
					"BOT: Adding note that file has been deleted on Commons");
	}
}