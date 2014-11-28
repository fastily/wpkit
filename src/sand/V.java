package sand;

import java.security.Provider;
import java.security.Security;

import commons.Commons;

import jwiki.core.MQuery;
import jwiki.core.Wiki;
import jwiki.util.WikiGen;

public class V
{
	public static void main(String[] args)
	{
		//for(Provider p : Security.getProviders())
		//	System.out.println(p);
		Wiki wiki = WikiGen.generate("Fastily");
		new Commons(wiki, wiki).emptyCatDel(wiki.getCategoryMembers("Category:Images from the Geograph British Isles project needing categories by grid square", "Category"));
		

	}
}