package mtc;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.dwrap.ImageInfo;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.FString;
import fastily.jwikix.tplate.ParsedItem;
import fastily.jwikix.tplate.Template;
import util.Toolbox;

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
	private static final Pattern captionRegex = Pattern.compile("(?si)\n?\\=\\=\\s*?(Caption).+?\\|\\}");

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
		else if (t != null && Toolbox.downloadFile(imgInfoL.get(0).url, localFN)
				&& com.upload(Paths.get(localFN), comFN, t, String.format(Config.tFrom, wpFN)))
			return enwp.addText(wpFN, String.format("{{subst:ncd|%s}}%n", comFN), Config.tTo, true);

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
			if (mtc.tpMap.containsKey(t.title))
				t.title = mtc.tpMap.get(t.title);

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
				case "Self":
					if (!t.has("author"))
						t.put("author", String.format("{{User at project|%s|w|en}}", uploader));
					break;
				case "PD-self":
					t.title = "PD-user-en";
					t.put("1", uploader);
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
		if (info == null)
			info = new Template("Information");

		if (!info.has("Description"))
			info.put("Description", "");
		if (!info.has("Date"))
			info.put("Date", "");
		if (!info.has("Source"))
			info.put("Source", String.format("{{Transferred from|en.wikipedia|%s|%s}}", enwp.whoami(), Config.mtcComLink));
		if (!info.has("Author"))
			info.put("Author", String.format("{{Original uploader|%s|w}}", uploader));
		if (!info.has("Permission"))
			info.put("Permission", "");
		if (!info.has("other versions"))
			info.put("other versions", "");

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
			x = m.group();
			sumSection = m.reset().replaceAll("");
			sumSection += "\n" + x;
		}
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
		t += "\n== {{Original upload log}} ==\n"
				+ String.format("{{Original description page|en.wikipedia|%s}}%n", FString.enc(enwp.nss(wpFN)))
				+ "{| class=\"wikitable\"\n! {{int:filehist-datetime}} !! {{int:filehist-dimensions}} !! {{int:filehist-user}} "
				+ "!! {{int:filehist-comment}}\n|-\n";

		for (ImageInfo ii : imgInfoL)
			t += String.format(uLFmt, dtf.format(LocalDateTime.ofInstant(ii.timestamp, ZoneOffset.UTC)), ii.dimensions.x, ii.dimensions.y,
					ii.user, ii.user, ii.summary.replace("\n", " "));
		t += "|}\n\n{{Subst:Unc}}";

		return t;
	}
}