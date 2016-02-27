/*
 * 
 */
package net.community.chest.ui.helpers.test;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.Collection;

import net.community.chest.awt.image.AbstractImageReader;
import net.community.chest.awt.image.BMPReader;
import net.community.chest.awt.image.ICOReader;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Nov 13, 2008 12:38:43 PM
 */
public class HelpersTester extends TestBase {
	public static final int testImageFilesReader (final PrintStream out, final BufferedReader in, final AbstractImageReader r, final File f)
	{
		if (f.isDirectory())
		{
			out.println("Entering " + f.getAbsolutePath() + " ...");

			final File[]	fa=f.listFiles();
			if ((null == fa) || (fa.length <= 0))
				return 0;

			for (final File ff : fa)
			{
				if (null == ff)
					continue;

				if (ff.isFile())
				{
					if (r.isMatchingFile(ff))
					{
						final int	nErr=testImageFilesReader(out, in, r, ff);
						if (nErr != 0)
							return nErr;
					}
				}
				else if (ff.isDirectory())
				{
					final int	nErr=testImageFilesReader(out, in, r, ff);
					if (nErr != 0)
						return nErr;
				}
			}

			out.println("Exiting " + f.getAbsolutePath() + " ...");
			return 0;
		}

		for ( ; ; )
		{
			out.println("Processing " + f.getAbsolutePath() + " ...");

			final long	pStart=System.currentTimeMillis();
			try
			{
				final Collection<? extends Image>	il=r.readImages(f);
				final long	pEnd=System.currentTimeMillis(), pDuration=pEnd - pStart;
				final int	numImages=(null == il) ? 0 : il.size();
				out.println("\tLoaded " + numImages + " in " + pDuration + " msec.");
			}
			catch(Exception e)
			{
				final long	pEnd=System.currentTimeMillis(), pDuration=pEnd - pStart;
				System.err.println(e.getClass().getName() + " after " + pDuration + " msec.: " + e.getMessage());
			}

			final String	ans=getval(out, in, "again [y]/n/q");
			if ((null == ans) || (ans.length() <= 0) || ('y' == Character.toLowerCase(ans.charAt(0))))
				continue;

			if (isQuit(ans))
				return (-1);

			break;
		}

		return 0;
	}

	// each argument is a file path to an ICO file or a directory to be traversed recursively
	public static final int testImageFilesReader (final PrintStream out, final BufferedReader in, final AbstractImageReader r, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	filePath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "ICO file path (or Quit)");
			if ((null == filePath) || (filePath.length() <= 0))
				continue;
			if (isQuit(filePath)) break;

			testImageFilesReader(out, in, r, new File(filePath));
		}

		return 0;
	}

	// each argument is a file path to an ICO file or a directory to be traversed recursively
	public static final int testICOFilesReader (final PrintStream out, final BufferedReader in, final String ... args)
	{
		return testImageFilesReader(out, in, ICOReader.DEFAULT, args);
	}

	// each argument is a file path to an ICO file or a directory to be traversed recursively
	public static final int testBMPFilesReader (final PrintStream out, final BufferedReader in, final String ... args)
	{
		return testImageFilesReader(out, in, new BMPReader(), args);
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		final BufferedReader	in=getStdin();
//		final int				nErr=testICOFilesReader(System.out, in, args);
		final int				nErr=testBMPFilesReader(System.out, in, args);
		if (nErr != 0)
			System.err.println("test failed (err=" + nErr + ")");
		else
			System.out.println("OK");
	}
}
