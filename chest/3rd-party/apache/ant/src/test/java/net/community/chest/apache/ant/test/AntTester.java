package net.community.chest.apache.ant.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Collection;

import net.community.chest.apache.ant.helpers.SkeletonBuildProject;
import net.community.chest.apache.ant.helpers.SkeletonBuildTarget;
import net.community.chest.test.TestBase;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jul 19, 2007 3:14:36 PM
 */
public class AntTester extends TestBase {

	//////////////////////////////////////////////////////////////////////////

	// args[i]=path of an ANT XML build file
	private static final int testSkeletonProjectReader (final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int					numArgs=(null == args) ? 0 : args.length;
		final SkeletonBuildProject	sp=new SkeletonBuildProject();

		for (int	aIndex=0; ; aIndex++)
		{
			final String path=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "XML file path (or Quit)");
			if ((null == path) || (path.length() <= 0))
				continue;
			if (isQuit(path)) break;

			for (String ans=null; ; )
			{
				out.println(path);

				try
				{
					sp.fromFilepath(path);

					out.println("\tProject name=" + sp.getName() + " default=" + sp.getDefaultTarget());
					final Collection<SkeletonBuildTarget>	targets=sp.getTargets();
					if (targets != null)
					{
						for (final SkeletonBuildTarget tgt : targets)
						{
							out.println("\t\t" + tgt);

							final Collection<String>	deps=tgt.getDependsList();
							if ((deps != null) && (deps.size() > 0))
							{
								for (final String d : deps)
									out.println("\t\t\t" + d);
							}
						}
					}
				}
				catch(Exception e)
				{
					System.err.println(e.getClass().getName() + " while parsing file=" + path + ": " + e.getMessage());
				}

				if ((null == (ans=getval(out, in, "again (y/[n]/q)"))) || (ans.length() <= 0))
					break;
				if (isQuit(ans)) return 0;

				if (Character.toLowerCase(ans.charAt(0)) != 'y')
					break;
			}
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		final BufferedReader	in=getStdin();
		final int				nErr=testSkeletonProjectReader(System.out, in, args);
		if (nErr != 0)
			System.err.println("test failed (err=" + nErr + ")");
		else
			System.out.println("OK");
	}
}
