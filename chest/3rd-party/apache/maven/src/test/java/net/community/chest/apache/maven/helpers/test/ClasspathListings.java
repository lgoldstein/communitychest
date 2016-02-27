package net.community.chest.apache.maven.helpers.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import org.w3c.dom.Element;

import net.community.chest.Triplet;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since May 1, 2008 5:25:46 PM
 */
public class ClasspathListings extends DownloadsListing {
	protected ClasspathListings ()
	{
		super();
	}

	private static final String	CPENTRY="<classpathentry";
	private static final int	CPELEN=CPENTRY.length();
	private static final String	CPE_PREFIX="M2_REPO/";

	private static final int showClasspathEntries (final String path, final PrintStream out, final BufferedReader fin) throws IOException
	{
		int	numEntries=0;

		out.println("================== " + path + " ==========================");
		out.println("Group,Artifact,Version,JAR");
		for (String l=StringUtil.getCleanStringValue(fin.readLine()); l != null; l = StringUtil.getCleanStringValue(fin.readLine()))
		{
			final int	ll=l.length();
			int			sPos=0;
			while ((sPos < ll) && (l.charAt(sPos) != '<'))
				sPos++;
			if (sPos >= (ll-1))
				continue;

			final String	cpe=l.substring(sPos);
			final int		cLen=cpe.length();
			if (cLen <= CPELEN)
				continue;

			final char	ch=cpe.charAt(CPELEN);
			if ((ch != ' ') && (ch != '\t'))
				continue;

			final String	kwVal=cpe.substring(0, CPELEN);
			if (!CPENTRY.equalsIgnoreCase(kwVal))
				continue;

			final Triplet<? extends Element,?,?>	pe=
				DOMUtils.parseElementString(cpe);
			final Element							elem=
				(null == pe) ? null : pe.getV1();
			final String							url=
				(null == elem) ? null : elem.getAttribute("path");
			if (!StringUtil.startsWith(url, CPE_PREFIX, true, true))
				continue;

			if (showData(out, url.substring(CPE_PREFIX.length()), '/'))
				numEntries++;
		}

		return numEntries;
	}

	// args[i]=the full path of a .classpath file
	private static final int showClasspathEntries (final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	path=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "Eclipse classpath file path (or Quit)");
			if ((null == path) || (path.length() <= 0))
				continue;
			if (isQuit(path)) break;
		
			try(BufferedReader	fin=new BufferedReader(new FileReader(path))) {
				showClasspathEntries(path, out, fin);
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + " while handling file=" + path + ": " + e.getMessage());
			}
		}

		return 0;
	}

	public static void main (String[] args)
	{
		showClasspathEntries(System.out, getStdin(), args);
	}
}
