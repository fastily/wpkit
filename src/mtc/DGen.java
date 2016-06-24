package mtc;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jwiki.core.MQuery;
import jwiki.core.Wiki;
import jwiki.dwrap.ImageInfo;
import jwiki.util.FL;
import jwiki.util.FString;
import jwikix.util.TParse;
import mtc.MTC.TransferObject;

/**
 * Generates a file description page for an enwp file to be transferred to Commons.
 * 
 * @author Fastily
 *
 */
public class DGen
{
	/**
	 * Matches an Information template parameter
	 */
	private static final Pattern infoParams = Pattern
			.compile("(?i)\\|\\s*?(Description|Source|Author|Date|Permission|other( |_)versions)\\s*?\\=");

	/**
	 * Matches caption sections in enwp text
	 */
	private static final Pattern captionRegex = Pattern.compile("(?si)\n?\\=\\=\\s*?(Caption).+?\\|\\}");

	/**
	 * The title for Info, Self, and MTC templates.
	 */
	private static final String tINFO = "Template:Information", tSELF = "Template:Self",
			tMTC = "Template:Copy to Wikimedia Commons";

	/**
	 * The Set of templates which should be handled specially.
	 */
	private static final HashSet<String> specialTL = FL.toSHS(tINFO, tSELF, tMTC);

	/**
	 * Format String for {{Information}}
	 */
	private static final String infoFmt = "{{Information%n|Description=%s%n|Date=%s%n|Source=%s%n|Author=%s%n|Permission=%s%n|other versions=%s%n}}%n";

	/**
	 * The format String for a row in the Upload Log section.
	 */
	private static final String uLFmt = "| %s || %d× %d× || [[w:User:%s|%s]] || ''<nowiki>%s</nowiki>''%n|-%n";

	/**
	 * A date formatter for UTC times.
	 */
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * The Wiki objects to use
	 */
	private Wiki enwp, com;

	/**
	 * The parent object of this DGen.
	 */
	private MTC mtc;

	/**
	 * Creates a new DGen object. This should be created for an MTC instance.
	 * 
	 * @param m The parent MTC object
	 */
	protected DGen(MTC m)
	{
		mtc = m;
		enwp = m.enwp;
		com = m.com;
	}

	/**
	 * Generates a file description page for the specified title
	 * 
	 * @param to The TransferObject to generate a file description page for
	 * @return The file description page.
	 */
	protected String generate(TransferObject to)
	{
		return new Desc(to).genText();
	}

	/**
	 * Represents a generated description for a file
	 * 
	 * @author Fastily
	 *
	 */
	private class Desc
	{
		/**
		 * The local enwp text of the file. May be modified via processing.
		 */
		private String t;

		/**
		 * Parsed values for the Information template.
		 */
		private HashMap<String, String> info = new HashMap<>();

		/**
		 * The list of templates on the local enwp page.
		 */
		private HashSet<String> tpl;

		/**
		 * The list of templates which will be added to the Licensing section.
		 */
		private ArrayList<String> sumSection = new ArrayList<>(), licSection = new ArrayList<>();

		/**
		 * The list of old revisions for the file
		 */
		private ArrayList<ImageInfo> imgInfoL;

		/**
		 * The full ('File:' prefix included) title of the file.
		 */
		private String title;

		/**
		 * Constructor, creates a Desc object
		 * 
		 * @param to The TransferObject to generate a description for.
		 */
		private Desc(TransferObject to)
		{
			this.title = to.wpFN;
			t = enwp.getPageText(title);
			tpl = new HashSet<>(enwp.getTemplatesOnPage(title));
			imgInfoL = to.ii;

			stripJunk();
			extractLics();
			parseInfo();
			extractMiscComponents();
		}

		/**
		 * Parses the Information template, if applicable.
		 */
		private void parseInfo()
		{
			if (!tpl.contains(tINFO))
				return;

			Pattern infoTP = mtc.regexMap.get(tINFO);
			String rawInfo = TParse.extractTemplate(infoTP, t);
			rawInfo = rawInfo.substring(2, rawInfo.length() - 2);

			Matcher m = infoParams.matcher(rawInfo);
			ArrayList<Integer> l = new ArrayList<>();
			while (m.find())
				l.add(m.start());

			ArrayList<String> plx = new ArrayList<>();
			for (int i = 0; i < l.size() - 1; i++)
				plx.add(rawInfo.substring(l.get(i), l.get(i + 1)));
			plx.add(rawInfo.substring(l.get(l.size() - 1)));

			for (String s : plx)
			{
				String[] pl = s.split("\\=", 2);

				String v = pl[1].trim();
				if (!v.isEmpty())
					info.put(pl[0].substring(1).trim().toLowerCase().replace('_', ' '), v);
			}

			t = infoTP.matcher(t).replaceAll(""); // remove {{Information}} since we're done
		}

		/**
		 * Extracts valid license templates from the page
		 */
		private void extractLics()
		{
			for (String tp : tpl)
				if (mtc.regexMap.containsKey(tp) && !specialTL.contains(tp))
				{
					String s = findAndReplace(mtc.regexMap.get(tp));
					licSection.add(s.isEmpty() ? String.format("{{%s}}", enwp.nss(tp)) : s);
				}
		}

		/**
		 * Strips junk that does not transfer well to Commons
		 */
		private void stripJunk()
		{
			t = t.replaceAll("(?i)\\n?\\[\\[(Category:).*?\\]\\]", ""); // categories don't transfer well.
			t = t.replaceAll("(?mi)^\\==.*?(Summary|Lic|filedesc).*?==$", ""); // strip headers
			t = t.replaceAll("(?<=\\[\\[)(.+?\\]\\])", "w:$1"); // add enwp prefix to links

			t = mtc.regexMap.get(tSELF).matcher(t).replaceAll("");
			t = mtc.regexMap.get(tMTC).matcher(t).replaceAll("");
		}

		/**
		 * Extract misc components, see section comments for details.
		 */
		private void extractMiscComponents()
		{
			// Keep non-lic templates used on Commons, strip the rest.
			for (Map.Entry<String, Boolean> e : MQuery.exists(com, FL.toAL(tpl.stream().filter(s -> !mtc.regexMap.containsKey(s))))
					.entrySet())
			{
				String s = findAndReplace(Pattern.compile(TParse.makeTitleRegex(FL.toSAL(enwp.nss(e.getKey())))));
				if (e.getValue() && !s.isEmpty())
					licSection.add(s);
			}

			// Extract captions
			String caption = findAndReplace(captionRegex);
			if (!caption.isEmpty())
				sumSection.add(caption);

			// t should be empty by now. If it is not, then add it to the section list
			t = t.trim();
			if (!t.isEmpty())
				info.put("description", info.containsKey("description") ? info.get("description") + "\n" + t : t);
		}

		/**
		 * Finds the specified pattern, extract it from <code>t</code>, and return it.
		 * 
		 * @param p The Pattern to match.
		 * @return The first String matching <code>p</code>, or the empty String if nothing was found.
		 */
		private String findAndReplace(Pattern p)
		{
			String x = "";
			Matcher m = p.matcher(t);
			if (m.find())
			{
				x = m.group();
				t = m.reset().replaceAll("");
			}

			return x;
		}

		/**
		 * Gets <code>info</code> value or the empty String if the value does not exist.
		 * 
		 * @param k The key to get a value for
		 * @return The value, or the empty String if key <code>k</code> was not found
		 */
		private String getInfo(String k)
		{
			return info.containsKey(k) ? info.get(k) : "";
		}

		/**
		 * Generates a Commons description page for this Desc object
		 * 
		 * @return The Commons file description page.
		 */
		private String genText()
		{
			String dump = "== {{int:filedesc}} ==\n";
			dump += String.format(infoFmt, getInfo("description"), getInfo("date"),
					info.containsKey("source") ? info.get("source")
							: String.format("{{Transferred from|en.wikipedia|%s|%s}}", enwp.whoami(), Config.mtcComLink),
					info.containsKey("author") ? info.get("author")
							: String.format("{{Original uploader|%s|w}}", imgInfoL.get(imgInfoL.size() - 1).user),
					getInfo("permission"), getInfo("other versions"));
			dump += String.join("\n", sumSection);
			dump += "\n== {{int:license-header}} ==\n";
			dump += String.join("\n", licSection);
			dump += "\n\n== {{Original upload log}} ==\n";
			dump += String.format("{{Original description page|en.wikipedia|%s}}%n", FString.enc(enwp.nss(title)));
			dump += "{| class=\"wikitable\"\n! {{int:filehist-datetime}} !! {{int:filehist-dimensions}} !! {{int:filehist-user}} !! {{int:filehist-comment}}\n|-\n";

			for (ImageInfo ii : imgInfoL)
				dump += String.format(uLFmt, dtf.format(LocalDateTime.ofInstant(ii.timestamp, ZoneOffset.UTC)), ii.dimensions.x,
						ii.dimensions.y, ii.user, ii.user, ii.summary.replace("\n", " "));

			dump += "|}\n\n{{Subst:Unc}}";

			return dump;
		}
	}
}