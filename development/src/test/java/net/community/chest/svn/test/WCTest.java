/*
 * 
 */
package net.community.chest.svn.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

import net.community.chest.svnkit.SVNLocation;
import net.community.chest.svnkit.core.io.SVNRepositoryFactoryType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;


/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 9:18:03 AM
 */
public class WCTest extends SVNBaseTest {
	public static final int testWCAccess (
			final PrintStream out, final BufferedReader in,
			final SVNWCClient wcc, final SVNLocation loc)
		throws Exception
	{
			out.println("Working on " + loc);

			final SVNURL	url=loc.getURL();
			if (url != null)
			{
				final SVNRepositoryFactoryType	t=SVNRepositoryFactoryType.setup(url);
				if (t != null)
					out.append("Initialized factory for ").append(t.name()).println();
			}

			for ( ; ; )
			{
				final String	ans=getval(out, in, "(A)dd/(D)elete/(C)ommit/(U)pdate/[I]nfo/(L)ist/Lo(g)/(D)elete/Dum(p)/(Q)uit");
				if (isQuit(ans)) break;

				final char	opChar=((ans == null) || (ans.length() <= 0)) ? '\0' : Character.toUpperCase(ans.charAt(0));
				try
				{
					switch(opChar)
					{
						case '\0'	:
						case 'I'	:
							{
								final SVNInfo	info=loc.getInfo(wcc);
								final String	strInfo=ToStringBuilder.reflectionToString(info, ToStringBuilder.getDefaultStyle());
								out.append('\t').println(strInfo);
							}
							break;
	
						case 'L'	:
							{
								final List<? extends SVNLocation>	locList=loc.listFiles(wcc);
								final int							numLocations=(locList == null) ? 0 : locList.size();
								if (numLocations > 0)
								{
									for (final SVNLocation child : locList)
										out.append('\t').append(child.getName()).println();
								}
								else
									out.println("\tNo children found");
							}
							break;

						case 'P'	:
							loc.copyTo(wcc, out);
							break;

						default		: // do nothing
					}
				}
				catch(Exception e)
				{
					System.err.println(e.getClass().getName() + ": " + e.getMessage());
				}
			}

			return 0;
	}

	/* -------------------------------------------------------------------- */

	// args[0]=username, args[1]=password, args[i]=an SVN location to be tested
	public static final int testWCAccess (
			final BufferedReader in, final PrintStream out, final String ... args)
	{
		final String[]	prompts={ "username", "password" },
						tstArgs=resolveTestParameters(out, in, args, prompts);
		if ((null == tstArgs) || (tstArgs.length < prompts.length))
			return 0;

		final String			username=tstArgs[0], password=tstArgs[1];
		final SVNClientManager	mgr=
			SVNClientManager.newInstance(
					SVNWCUtil.createDefaultOptions(null, false),
					SVNWCUtil.createDefaultAuthenticationManager(username, password));
		final int				numArgs=(null == tstArgs) ? 0 : tstArgs.length;
		for (int	aIndex=tstArgs.length-1; ; aIndex++)
		{
			final String	path=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "WC/URL path (or Quit)");
			if ((null == path) || (path.length() <= 0))
				continue;
			if (isQuit(path)) break;
		
			try
			{
				testWCAccess(out, in, mgr.getWCClient(), SVNLocation.fromString(path));
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + " while handle file=" + path + ": " + e.getMessage());
			}
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		testWCAccess(getStdin(), System.out, args);
	}
}
