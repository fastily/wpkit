package ft;

import javax.security.auth.login.LoginException;

import commons.Commons;
import jwiki.core.Wiki;
import jwiki.util.WikiGen;

public class XZ
{
	public static final Wiki fastily = WikiGen.generate("Fastily");

	public static final Wiki clone = WikiGen.generate("FastilyClone");

	public static final Commons com = new Commons(clone, fastily);

	public static void main(String[] args) throws LoginException
	{
		Wiki wiki = new Wiki("Your_Username", "Your_Password", "en.wikipedia.org");
		
		wiki.edit("User:YourUserPage", "SomeRandomText", "EditSummary");
	}
}