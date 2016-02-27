/*
 * 
 */
package net.community.chest.aspectj.test;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;

import net.community.chest.test.TestBase;

/**
 * @author Lyor G.
 * @since Jul 19, 2010 2:45:58 PM
 *
 */
public class FileInputAspectTester extends TestBase {
	public FileInputAspectTester ()
	{
		super();
	}

	/* -------------------------------------------------------------------- */

	public static final void testFileInputAspect (
			final BufferedReader in, final PrintStream out, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	filePath=
				(aIndex < numArgs) ? args[aIndex] : getval(out, in, "file path string (or Quit)");
			if ((null == filePath) || (filePath.length() <= 0))
				continue;
			if (isQuit(filePath)) break;

			final String	ctorType=getval(out, in, "[S]tream/(R)eader/R(a)ndom/(Q)uit for path=" + filePath);
			if (isQuit(ctorType)) continue;

			final char	ct=((null == ctorType) || (ctorType.length() <= 0)) ? '\0' : ctorType.charAt(0);
			try
			{
				final Closeable	fin;
				switch(ct)
				{
					case '\0'	:
					case 's'	:
					case 'S'	:
						fin = new FileInputStream(new File(filePath));
						break;

					case 'r'	:
					case 'R'	:
						fin = new FileReader(new File(filePath));
						break;

					case 'a'	:
					case 'A'	:
						fin = new RandomAccessFile(new File(filePath), "r");
						break;

					default		:
						fin = null;
				}

				if (fin != null)
					fin.close();
			}
			catch(Exception e)
			{
				System.err.append(e.getClass().getName())
				  .append(" while handle file=").append(filePath)
				  .append(": ").append(e.getMessage())
				  .println();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		testFileInputAspect(getStdin(), System.out, args);
	}
}
