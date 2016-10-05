package mtc;

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
import fastily.jwikix.tplate.ParsedItem;
import fastily.jwikix.tplate.Template;
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
		return new Desc(to).toString();
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
		 * The user who originally uploaded the file.  Excludes <code>User:</code> prefix.
		 */
		private String uploader;

		/**
		 * Constructor, creates a Desc object
		 * 
		 * @param to The TransferObject to generate a description for.
		 */
		private Desc(TransferObject to)
		{
			root = ParsedItem.parse(enwp, to.wpFN);
			
			imgInfoL = to.ii;
			uploader = imgInfoL.get(imgInfoL.size() - 1).user;

			procText();
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
			for(Template t : new ArrayList<>(tpl))
				if (ncomT.contains(t.title))
					tpl.remove(t.drop());
			
			// Process special Templates
			Template info = null;
			for(Template t : new ArrayList<>(tpl))
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
			for(Template t : tpl)
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
			if(!root.contents.isEmpty())
				info.append("Description", " " + String.join(" ", root.contents));
			
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
		 * Generates an upload log for this Desc.
		 * @return An upload log section for this Desc.
		 */
		private String genUploadLog()
		{
			String s = "\n== {{Original upload log}} ==\n{| class=\"wikitable\"\n! {{int:filehist-datetime}} !!"
					+ " {{int:filehist-dimensions}} !! {{int:filehist-user}} !! {{int:filehist-comment}}\n|-\n";
			
			for (ImageInfo ii : imgInfoL)
				s += String.format(uLFmt, dtf.format(LocalDateTime.ofInstant(ii.timestamp, ZoneOffset.UTC)), ii.dimensions.x,
						ii.dimensions.y, ii.user, ii.user, ii.summary.replace("\n", " "));
			s += "|}\n\n{{Subst:Unc}}";
			
			return s;
		}
	
		/**
		 * Renders this Desc as wikitext for Commons.
		 */
		public String toString()
		{
			String t = sumSection + licSection;
			
			t = t.replaceAll("(?s)\\<!\\-\\-.*?\\-\\-\\>", ""); // strip comments
			t = t.replaceAll("(?i)\\n?\\[\\[(Category:).*?\\]\\]", ""); // categories don't transfer well.
			t = t.replaceAll("(?<=\\[\\[)(.+?\\]\\])", "w:$1"); // add enwp prefix to links
			t = t.replace("[[w::", "[[w:"); // Remove any double colons in interwiki links
			
			return t + genUploadLog();
		}
	}
}