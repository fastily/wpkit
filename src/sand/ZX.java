package sand;

import commons.CStrings;
import commons.Commons;
import jwiki.core.Wiki;
import jwiki.util.WikiGen;

public class ZX
{
	protected static final Wiki clone = WikiGen.generate("FastilyClone");

	protected static final Wiki admin = WikiGen.generate("Fastily");

	protected static final Commons com = new Commons(clone, admin);

	public static void main(String[] args)
	{
		for (String s : admin.getCategoryMembers(CStrings.osd, "Category talk"))
			admin.delete(s, CStrings.test);
	}
}