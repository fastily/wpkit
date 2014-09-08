package sand;

import java.util.ArrayList;

import commons.CStrings;
import commons.Commons;
import jwiki.core.Contrib;
import jwiki.core.Wiki;
import jwiki.util.ReadFile;
import jwiki.util.WikiGen;

public class ZX
{
	protected static final Wiki clone = WikiGen.generate("FastilyClone");

	protected static final Wiki admin = WikiGen.generate("Fastily");

	protected static final Commons com = new Commons(clone, admin);

	public static void main(String[] args)
	{
		

		for(String s : clone.getCategoryMembers(CStrings.osd, "File"))
			if(clone.getPageText(s).contains("COM:FOP#Turkmenistan"))
				admin.delete(s, CStrings.dw);
	
	}
}