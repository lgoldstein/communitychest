/*
 * 
 */
package net.community.chest.io.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import net.community.chest.io.FileUtil;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 2, 2009 10:59:39 AM
 */
public class FileAdjust extends TestBase {
	private static final void fileAdjust (
			final File fin, final File fout, final int numChars) throws IOException
	{
		BufferedReader	r=null;
		PrintStream		w=null;
		try
		{
			r = new BufferedReader(new FileReader(fin));
			w = new PrintStream(new FileOutputStream(fout));

			for (String	line=r.readLine(); line != null; line=r.readLine())
			{
				final int		ll=line.length();
				final String	ol=(ll > numChars) ? line.substring(numChars) : "";
				w.println(ol);
			}
		}
		finally
		{
			FileUtil.closeAll(r, w);
		}
	}
	// args[0]=source folder, args[1]=destination folder
	private static final void fileAdjust (
			final PrintStream out, final BufferedReader in, final String ... args) throws IOException
	{
		final String[]	prompts={ "source file", "destination file" },
						files=resolveTestParameters(out, in, args, prompts);
		if ((null == files) || (files.length < prompts.length))
			return;

		fileAdjust(new File(files[0]), new File(files[1]), 8);
	}

	public static void main (String[] args)
	{
		try
		{
			fileAdjust(System.out, getStdin(), args);
		}
		catch(Exception e)
		{
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

}
