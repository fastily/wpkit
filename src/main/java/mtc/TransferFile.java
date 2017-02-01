package mtc;

import java.net.URLEncoder;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ctools.tplate.ParsedItem;
import ctools.tplate.Template;
import ctools.util.Toolbox;
import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.dwrap.ImageInfo;
import fastily.jwiki.util.FL;

/**
 * Represents a file to transfer to Commons
 * 
 * @author Fastily
 *
 */
public class TransferFile
{
	/**
	 * Matches caption sections in enwp text
	 */
	private static final Pattern captionRegex = Pattern.compile("(?si)\\{\\|\\s*?class\\=\"wikitable.+?\\|\\}");

	/**
	 * The format String for a row in the Upload Log section.
	 */
	private static final String uLFmt = "| %s || %d× %d× || [[w:User:%s|%s]] || ''<nowiki>%s</nowiki>''%n|-%n";

	/**
	 * A date formatter for UTC times.
	 */
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * The enwp filename
	 */
	protected final String wpFN;

	/**
	 * The commons filename and local path
	 */
	private final String comFN, localFN;

	/**
	 * The Wiki objects to use
	 */
	private Wiki enwp, com;

	/**
	 * The parent MTC Object that owns this TransferFile.
	 */
	private MTC mtc;

	/**
	 * The root ParsedItem for the file's enwp page.
	 */
	private ParsedItem root;

	/**
	 * The summary and license sections.
	 */
	private String sumSection = "== {{int:filedesc}} ==\n", licSection = "\n== {{int:license-header}} ==\n";

	/**
	 * The list of old revisions for the file
	 */
	private ArrayList<ImageInfo> imgInfoL;

	/**
	 * The user who originally uploaded the file. Excludes <code>User:</code> prefix.
	 */
	private String uploader;

	/**
	 * Constructor, creates a TransferObject
	 * 
	 * @param wpFN The enwp title to transfer
	 * @param comFN The commons title to transfer to
	 */
	protected TransferFile(String wpFN, String comFN, MTC mtc)
	{
		this.enwp = mtc.enwp;
		this.com = mtc.com;
		this.comFN = comFN;
		this.wpFN = wpFN;
		this.mtc = mtc;

		String baseFN = enwp.nss(wpFN);
		localFN = Config.fdump + baseFN.hashCode() + baseFN.substring(baseFN.lastIndexOf('.'));
	}

	/**
	 * Attempts to transfer an enwp file to Commons
	 * 
	 * @return True on success.
	 */
	protected boolean doTransfer()
	{
		try
		{
			root = ParsedItem.parse(enwp, wpFN);

			imgInfoL = enwp.getImageInfo(wpFN);
			uploader = imgInfoL.get(imgInfoL.size() - 1).user;

			procText();
			String t = gen();

			if (mtc.dryRun)
			{
				System.out.println(t);
				return true;
			}
			else if (t != null && Toolbox.downloadFile(enwp.apiclient.client, imgInfoL.get(0).url.toString(), localFN)
					&& com.upload(Paths.get(localFN), comFN, t, String.format(Config.tFrom, wpFN)))
				return enwp.edit(wpFN, String.format("{{subst:ncd|%s|reviewer=%s}}%n", comFN, enwp.whoami()) + enwp.getPageText(wpFN).replaceAll(mtc.mtcRegex, ""), Config.tTo);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Processes parsed text and templates from the API
	 */
	private void procText()
	{
		ArrayList<Template> masterTPL = root.getTemplateR();

		// Normalize license and special template titles
		for (Template t : masterTPL)
		{
			String tp = enwp.whichNS(t.title).equals(NS.TEMPLATE) ? enwp.nss(t.title): t.title;
			if (mtc.tpMap.containsKey(tp))
				t.title = mtc.tpMap.get(tp);
		}

		ArrayList<Template> tpl = new ArrayList<>(masterTPL);

		// Filter Templates which are not on Commons
		HashSet<String> ncomT = FL
				.toSet(MQuery.exists(com, false, FL.toAL(tpl.stream().map(t -> "Template:" + t.title))).stream().map(com::nss));
		
		for (Template t : new ArrayList<>(tpl))
			if (ncomT.contains(t.title))
				tpl.remove(t.drop());

		// Process special Templates
		Template info = null;
		for (Template t : new ArrayList<>(tpl))
			switch (t.title)
			{
				case "Information":
					info = t;
					tpl.remove(t.drop());
					break;
				case "Multilicense replacing placeholder new":
				case "Self":
					if (!t.has("author"))
						t.put("author", String.format("{{User at project|%s|w|en}}", uploader));
					break;
				case "PD-self":
					t.title = "PD-user-en";
					t.put("1", uploader);
					break;
				case "GFDL-self-with-disclaimers":
					t.title = "GFDL-user-en-with-disclaimers";
					t.put("1", uploader);
					break;
				case "GFDL-self":
					t.title = "GFDL-self-en";
					t.put("author", String.format("{{User at project|%s|w|en}}", uploader));
					break;
				case "Copy to Wikimedia Commons":
					tpl.remove(t.drop());
					break;
				default:
					break;
			}

		// Add any Commons-compatible top-level templates to License section.
		tpl.retainAll(root.tplates);
		for (Template t : tpl)
			licSection += String.format("%s%n", t);

		// Create and fill in missing {{Information}} fields with default values.
		info = filterFillInfo(info == null ? new Template("Information") : info);

		// Append any additional Strings to the description.
		if (!root.contents.isEmpty())
			info.append("Description", "\n" + String.join("\n", root.contents));

		// Convert {{Information}} to String and save result.
		sumSection += info.toString(true) + "\n";

		// Extract the first caption table and move it to the end of the sumSection
		String x = "";
		Matcher m = captionRegex.matcher(sumSection);
		if (m.find())
		{
			x = m.group().trim();
			sumSection = m.reset().replaceAll("");
			sumSection += x + "\n";
		}
	}

	/**
	 * Filters nonsense {{Information}} parameters and fills in missing default values.
	 * 
	 * @param info The information template
	 * @return A new Template with filtered keys and default values where applicable
	 */
	private Template filterFillInfo(Template info)
	{
		Template t = new Template("Information");

		t.put("Description", info.has("Description") ? info.get("Description") : "");
		t.put("Date", info.has("Date") ? info.get("Date") : "");
		t.put("Source", info.has("Source") ? info.get("Source")
				: String.format("{{Transferred from|en.wikipedia|%s|%s}}", enwp.whoami(), Config.mtcComLink));
		t.put("Author", info.has("Author") ? info.get("Author") : "");
		t.put("Permission", info.has("Permission") ? info.get("Permission") : "");
		t.put("other versions", info.has("other versions") ? info.get("other versions") : "");

		return t;
	}

	/**
	 * Renders this TransferFile as wikitext for Commons.
	 */
	private String gen()
	{
		String t = sumSection + licSection;

		t = t.replaceAll("(?s)\\<!\\-\\-.*?\\-\\-\\>", ""); // strip comments
		t = t.replaceAll("(?i)\\n?\\[\\[(Category:).*?\\]\\]", ""); // categories don't transfer well.
		t = t.replaceAll("(?<=\\[\\[)(.+?\\]\\])", "w:$1"); // add enwp prefix to links
		t = t.replaceAll("(?i)\\[\\[(w::|w:w:)", "[[w:"); // Remove any double colons in interwiki links

		// Generate Upload Log Section
		try
		{
		t += "\n== {{Original upload log}} ==\n"
				+ String.format("{{Original description page|en.wikipedia|%s}}%n", URLEncoder.encode(enwp.nss(wpFN), "UTF-8"))
				+ "{| class=\"wikitable\"\n! {{int:filehist-datetime}} !! {{int:filehist-dimensions}} !! {{int:filehist-user}} "
				+ "!! {{int:filehist-comment}}\n|-\n";
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		
		for (ImageInfo ii : imgInfoL)
			t += String.format(uLFmt, dtf.format(LocalDateTime.ofInstant(ii.timestamp, ZoneOffset.UTC)), ii.dimensions.x, ii.dimensions.y,
					ii.user, ii.user, ii.summary.replace("\n", " "));
		t += "|}\n\n{{Subst:Unc}}";

		if(mtc.useTrackingCat)
			t += "\n[[Category:Uploaded with MTC!]]";
		
		return t;
	}
}