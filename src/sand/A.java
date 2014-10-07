package sand;


import commons.Commons;
import jwiki.core.Wiki;
import jwiki.util.WikiGen;

public class A
{
	public static void main(String[] args)
	{		
		Wiki wiki = WikiGen.generate("FastilyClone");
		Commons m = new Commons(wiki, WikiGen.generate("Fastily"));
		
		m.removeLSP("-", wiki.getUserUploads("Lovelybuter"));
		for(String s : wiki.getUserUploads("Lovelybuter"))
			wiki.addText(s, "{{Subst:Npd}}\n", "reset", true);
		 
	}
}