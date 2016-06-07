package mtc;

import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jwiki.core.ColorLog;
import jwiki.core.Req;
import jwiki.core.Wiki;
import jwiki.util.FString;
import jwikix.util.StrTool;

/**
 * Generates Commons description pages for enwp files transferred to
 * 
 * @author Fastily
 *
 */
public class TGen
{
	/**
	 * Matches vomit that CommonsHelper doesn't strip.
	 */
	private static final String uselessT = String.format("(?i)\\{\\{(%s)\\}\\}\n?",
			FString.pipeFence("Green", "Red", "Yesno", "Center", "Own", "Section link", "Trademark", "Bad JPEG", "Spoken article entry",
					"PD\\-BritishGov", "Convert", "Cc\\-by\\-sa", "Infosplit", "Cite book", "Trim", "Legend", "Hidden begin", "Hidden end",
					"Createdwith", "Main other", "OTRS permission", "Category handler"));

	/**
	 * Matches GFDL-disclaimers templates
	 */
	private static final Pattern gfdlDiscl = Pattern.compile("(?i)\\{\\{GFDL\\-user\\-(w|en)\\-(with|no)\\-disclaimers");

	/**
	 * Matches caption sections
	 */
	private static final Pattern captionRegex = Pattern.compile("(?si)\n?\\=\\=\\s*?(Caption).+?\\|\\}");

	/**
	 * Matches infosplit templates
	 */
	private static final Pattern infoSplitT = Pattern.compile("(?si)\\{\\{(infosplit).+?\\}\\}");

	/**
	 * The of enwp usages of InfoSplit
	 */
	private final ArrayList<String> infoSplitUses;

	/**
	 * A Wiki logged into enwp
	 */
	private final Wiki enwp;

	/**
	 * Creates a new TGen instance
	 * 
	 * @param enwp A Wiki logged into enwp
	 */
	protected TGen(Wiki enwp)
	{
		this.enwp = enwp;
		infoSplitUses = enwp.whatTranscludesHere("Template:Infosplit");
	}

	/**
	 * Generates text for the specified TransferObject
	 * 
	 * @param to The TransferObject to generate text for
	 * @return The Commons text for the TransferObject
	 */
	protected String generateText(TransferObject to)
	{
		ColorLog.fyi("Downloading text for " + to.wpFN);

		String t;

		// generate raw file desc text for Commons
		try
		{
			String rawhtml = FString.inputStreamToString(
					Req.genericPOST(new URL(Config.chURL), null, Req.urlenc, String.format(Config.posttext, FString.enc(to.baseFN))));
			t = rawhtml.substring(rawhtml.indexOf("{{Info"), rawhtml.indexOf("</textarea>"));

		}
		catch (Throwable e)
		{
			return null;
		}

		// cleanup text
		t = t.replaceAll(uselessT, "");
		t = t.replaceAll("(?si)\\{\\{(\\QCc-by-sa-3.0-migrated\\E|Copy to Commons|Do not move to Commons).*?\\}\\}\n?", "");
		t = t.replaceAll("\\Q<!--\\E.*?\\Q-->\\E\n?", "");
		t = t.replaceAll("(?si)\\|(Permission)\\=.*?\\|other_versions\\=", "|Permission=\n|other versions=");
		t = t.replaceAll("(?i)\\|(Source)\\=(Transferred from).*?\n", "|Source={{Transferred from|en.wikipedia}}\n");
		t = t.replaceAll("__NOTOC__\n?", "");
		t = t.replace("Original uploader was {{user at project", "{{Original uploader");
		t = t.replace("&times;", "×");
		
		// CommonsHelper bug?  Strip mangled self-gfdl licenses
		t = t.replaceAll("(?i)\\s?\\|GFDL.*?\\|migration.+?\\}{4}", "}}"); 
		t = t.replaceAll("(?i)\\{\\{self\\|author\\=\\{\\{.+?\\}\\}\n?", "");
		
		t = t.replace("{{en|}}", "");
		t = t.replaceAll("\\<br/\\>\n?", " ");
				
		if (!t.contains("int:filedesc"))
			t = "== {{int:filedesc}} ==\n" + t;

		// fix malformed GFDL-disclaimers templates
		Matcher m = gfdlDiscl.matcher(t);
		if (m.find()) // TODO: WTF is going on here
			try
			{
				t = String.format("%s|1=%s%s", t.substring(0, m.end()), enwp.getRevisions(to.wpFN, 1, true, null, null).get(0).user,
						t.substring(m.end()));
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}

		// fix malformed caption headers
		if ((m = captionRegex.matcher(t)).find())
		{
			String capt = m.group(); // copy caption section
			t = m.replaceAll(""); // strip
			t += capt; // dump it at the end
		}

		// Fixing missing {{Infosplit}}
		if (infoSplitUses.contains(to.wpFN) && (m = infoSplitT.matcher(enwp.getPageText(to.wpFN))).find())
		{
			String isplit = m.group();
			t = StrTool.insertAt(t, "\n" + isplit, t.indexOf("== {{int:license-header}} ==") - 2);
		}

		return t;
	}
}