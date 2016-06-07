package mtc;

import static mtc.Config.fdump;

import java.nio.file.Paths;

import jwiki.core.WTask;

/**
 * Represents an enwp file prepared for transfer to Commons.
 * 
 * @author Fastily
 *
 */
public class TransferObject
{
	/**
	 * The enwp, basefilename (just basename) filenames
	 */
	protected final String wpFN, baseFN;

	/**
	 * The commons and local filenames
	 */
	private final String comFN, localFN;

	/**
	 * This TransferObject's parent MTC instance.
	 */
	private MTC m;

	/**
	 * Constructor, creates a TransferObject
	 * 
	 * @param wpFN The enwp title to transfer
	 * @param comFN The commons title to transfer to
	 */
	protected TransferObject(String wpFN, String comFN, MTC m)
	{
		this.comFN = comFN;
		this.wpFN = wpFN;
		baseFN = m.enwp.nss(wpFN);
		localFN = fdump + baseFN;

		this.m = m;
	}

	/**
	 * Attempts to transfer an enwp file to Commons
	 * 
	 * @return True on success.
	 */
	protected boolean doTransfer()
	{
		String t = m.tg.generateText(this);

		if (m.dryRun)
		{
			System.out.println(t);
			return true;
		}
		else if (t != null && WTask.downloadFile(wpFN, localFN, m.enwp)
				&& m.com.upload(Paths.get(localFN), comFN, t, String.format(Config.tFrom, wpFN)))
			return m.enwp.edit(wpFN, String.format("{{subst:ncd|%s}}%n", comFN) + m.enwp.getPageText(wpFN).replaceAll(m.tRegex, ""),
					Config.tTo);

		return false;
	}

}