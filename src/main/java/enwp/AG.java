package enwp;

import com.google.gson.JsonElement;

import ctools.tplate.ParsedItem;
import ctools.util.Toolbox;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.GSONP;

public class AG
{
	public static void main(String[]args)
	{
		/*Wiki wiki = Toolbox.getFastily();
		
//		wiki.edit("User:Fastily/TestFoo", "test", "this is a quick test");
		System.out.println(wiki.getRevisions("Wikipedia:Sandbox", 1, false, null, null));
		
		for(String s : wiki.getCategoryMembers("Category:Candidates for speedy deletion as abandoned AfC submissions"))
			if(s.startsWith("Draft:") && wiki.getRevisions(s, 1, false, null, null).get(0).user.equals("1989"))
			{
				wiki.delete(s, "[[WP:CSD#G13|G13]]: Abandoned [[Wikipedia:Articles for creation|AfC]] submission – If you wish to retrieve it, please see [[WP:REFUND/G13]]");
//				break;
			}
		
		System.out.println("Hello!");*/
		
		JsonElement e=GSONP.jp.parse("{\"test\":5}");
		String s = e.getAsJsonObject().get("test").toString();
		System.out.println(s);
		
		Wiki wiki = Toolbox.getFastilyClone();
		System.out.println(ParsedItem.parse(wiki, "User:Fastily/TestX"));
		
	}
}