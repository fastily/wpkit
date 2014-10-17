package sand;


import java.util.ArrayList;

import commons.Commons;
import jwiki.core.MQuery;
import jwiki.core.Wiki;
import jwiki.util.WikiGen;

public class A
{
	public static void main(String[] args)
	{		
	
		
		Wiki wiki = WikiGen.generate("FastilyClone");
		Commons m = new Commons(wiki, WikiGen.generate("Fastily"));
		
		ArrayList<String> l = new ArrayList<>();
		for(int i = 1; i <= 15; i++)
			l.add(String.format("File:Futbala matĉo inter la Selektitaro de Esperanto kaj la Armen-devena Argentina Komunumo %02d.jpg", i));
		
		while(!m.restore("otrs", MQuery.exists(wiki, false, l)).isEmpty())
			;
		
		for(String s : l)
			wiki.replaceText(s, "(?si)\\{\\{(OTRS pending|copyvio).*?\\}\\}", "{{Subst:Npd}}", "reset");
		
		
		
		/*
		m.removeLSP("-", wiki.getUserUploads("Lovelybuter"));
		for(String s : wiki.getUserUploads("Lovelybuter"))
			wiki.addText(s, "{{Subst:Npd}}\n", "reset", true);*/
		 
	}
}