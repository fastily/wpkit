package sand;

import javax.security.auth.login.LoginException;

import jwiki.core.Wiki;

/**
 * This program will edit an article by replacing the article text with some text of your choosing.
 * 
 * @author Fastily
 *
 */
public class JwikiExample
{
	public static void main(String[] args) throws LoginException
	{
		Wiki wiki = new Wiki("Your_Username", "Your_Password", "en.wikipedia.org");
		wiki.edit("SomeArticle", "SomeRandomText", "EditSummary");
	}
}