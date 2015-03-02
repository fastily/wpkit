package commons;

/**
 * Commonly used constant strings for Commons work.
 * 
 * @author Fastily
 * 
 */
public class CStrings
{
	/**
	 * List of deletion reasons
	 * 
	 * @author Fastily
	 *
	 */
	public enum Reason
	{
		/**
		 * Uploader requested deletion of file
		 */
		ur("Uploader requested deletion of a recently uploaded and unused file"),

		/**
		 * Email OTRS to get file restored.
		 */
		baseP(
				"If you are the copyright holder/author and/or have authorization to publish the file, please email our [[Commons:OTRS|OTRS team]] to get the file restored"),

		/**
		 * Copyvio
		 */
		copyvio("[[Commons:Licensing|Copyright violation]]: " + baseP.rsn),

		/**
		 * Derivative work
		 */
		dw("[[Commons:Derivative works|Derivative]] of non-free content"),

		/**
		 * Orphaned file talk page
		 */
		oft("Orphaned File talk page"),

		/**
		 * Out of scope
		 */
		oos("Out of [[Commons:Project scope|project scope]]"),

		/**
		 * Housekeeping
		 */
		house("Housekeeping or non-controversial cleanup"),

		/**
		 * Fair use material is not allowed
		 */
		fu("[[Commons:Fair use|Fair use]] material is not permitted on Wikimedia Commons"),

		/**
		 * Empty category.
		 */
		ec("Empty [[Commons:Categories|category]]"),

		/**
		 * File page with no file uploaded.
		 */
		nfu("File page with no file uploaded"),

		/**
		 * Empty gallery
		 */
		eg("Empty or single image gallery; please see [[Commons:Galleries]]"),

		/**
		 * Reason for test page.
		 */
		test("Test page or page with no valid content"),

		/**
		 * User requested in own userspace.
		 */
		uru("User requested deletion in own [[Commons:Userpage|userspace]]"),

		/**
		 * Author requested deletion of page
		 */
		ar("Author requested deletion of page");

		/**
		 * The full text reason.
		 */
		public final String rsn;

		/**
		 * Constructor
		 * 
		 * @param rsn The reason.
		 */
		private Reason(String rsn)
		{
			this.rsn = rsn;
		}
	}

	/**
	 * List of frequently used categories. "Category:" prefix not included!
	 * 
	 * @author Fastily
	 *
	 */
	public enum Category
	{
		/**
		 * Other speedy deletions.
		 */
		osd("Other speedy deletions"),

		/**
		 * Category name for "Copyright violations", without the "Category:" prefix.
		 */
		cv("Copyright violations");

		/**
		 * The full text category
		 */
		public final String cat;

		/**
		 * Constructor
		 * 
		 * @param cat The category name, without "Category:" prefix.
		 */
		private Category(String cat)
		{
			this.cat = cat;
		}
	}

	/**
	 * Frequently used Regular Expressions
	 * 
	 * @author Fastily
	 *
	 */
	public enum Regex
	{
		/**
		 * Regex that matches deletion templates on Commons.
		 */
		delregex(
				"(?si)\\s??\\{\\{(speedy|fair use delete|speedydelete|no permission|no license|no source|copyvio|OTRS pending).*?\\}\\}\\s??"),

		/**
		 * Regex that matches DR templates on Commons.
		 */
		drregex("(?si)\\s??\\{\\{(db|delete).*?\\}\\}\\s??");

		/**
		 * The full text reason for this enum item
		 */
		public final String rgx;

		/**
		 * Constructor
		 * 
		 * @param rgx The regex.
		 */
		private Regex(String rgx)
		{
			this.rgx = rgx;
		}
	}
}