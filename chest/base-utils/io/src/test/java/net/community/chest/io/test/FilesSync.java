/*
 * 
 */
package net.community.chest.io.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.test.TestBase;
import net.community.chest.util.datetime.TimeUnits;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 15, 2009 1:44:06 PM
 */
public class FilesSync extends TestBase {
	private static final DateFormat	DTF=new SimpleDateFormat();

	public static final Collection<Map.Entry<File,File>> dirSync (
			final PrintStream out, final Collection<Map.Entry<File,File>> org, final File srcFolder, final File dstFolder) throws IOException
	{
		if ((null == srcFolder) || (null == dstFolder))
			return org;
		if ((!srcFolder.exists()) || (!srcFolder.isDirectory()))
			return org;

		final String[]	srcFiles=srcFolder.list();
		if ((null == srcFiles) || (srcFiles.length <= 0))
			return org;

		if (!dstFolder.exists())
		{
			if (!dstFolder.mkdirs())
				throw new IOException("Cannot create destination folder=" + dstFolder);

			out.println("Created " + dstFolder);
		}
		else if (!dstFolder.isDirectory())
			throw new IOException("Destination is not a folder: " + dstFolder);

		Collection<Map.Entry<File,File>>	ret=org;
		for (final String	sName : srcFiles)
		{
			final File	sFile=new File(srcFolder, sName), dFile=new File(dstFolder, sName);
			if (sFile.isDirectory())
			{
				ret = dirSync(out, ret, sFile, dFile);
				continue;
			}

			if (sFile.length() <= 0L)
			{
				out.println("Skip empty file " + sFile);
				if (dFile.exists())
				{
					if (dFile.delete())
						out.println("\tDeleted " + dFile);
					else
						System.err.println("Failed to delete " + dFile);
				}

				continue;
			}

			if (dFile.exists())
			{
				final long	sMod=sFile.lastModified(), dMod=dFile.lastModified(), mDiff=sMod - dMod;
				if (mDiff <= TimeUnits.MINUTE.getMilisecondValue())
				{
					if (mDiff != 0L)
					{
						final String	sDate=DTF.format(new Date(sMod)), dDate=DTF.format(new Date(dMod));
						out.println("\tSkip more recent file " + dFile + " (" + sDate + " vs. " + dDate + ")");
					}

					continue;
				}

				final long	sLen=sFile.length(), dLen=dFile.length();
				if (sLen == dLen)
				{
					out.println("\tSkip equal size file " + dFile);
					continue;
				}
			}
			else
				out.println("\tCopy new file " + dFile);

			out.print("Copy " + sFile + " => " + dFile);

			final long	cpyStart=System.currentTimeMillis(),
						cpyLen=IOCopier.copyFile(sFile, dFile),
						cpyEnd=System.currentTimeMillis(), cpyDuration=cpyEnd - cpyStart;
			if (cpyLen < 0L)
				throw new StreamCorruptedException("Error (" + cpyLen + ") after " + cpyDuration + " msrc. while copying " + sFile + " to " + dFile);
			out.println(" - " + cpyLen + " bytes in " + cpyDuration + " msrc.");

			if (null == ret)
				ret = new LinkedList<Map.Entry<File,File>>();
			ret.add(new MapEntryImpl<File,File>(sFile, dFile));
		}

		// make sure all destination files appear in source folder
		final String[]	dstFiles=dstFolder.list();
		if ((null == dstFiles) || (dstFiles.length <= 0))
			return ret;	// should not happen

		final Collection<String>	srcSet=new HashSet<String>(Arrays.asList(srcFiles));
		for (final String d : dstFiles)
		{
			if (srcSet.contains(d))
				continue;

			final File	dFile=new File(dstFolder, d);
			if (dFile.delete())
				out.println("\tDeleted " + dFile);
			else
				System.err.println("Failed to delete " + dFile);
		}

		return ret;
	}

	// args[0]=source folder, args[1]=destination folder
	public static final Collection<Map.Entry<File,File>> fileSync (
			final PrintStream out, final BufferedReader in, final String ... args) throws IOException
	{
		final String[]	prompts={ "source folder", "destination folder" },
						files=resolveTestParameters(out, in, args, prompts);
		if ((null == files) || (files.length < prompts.length))
			return null;

		return dirSync(out, null, new File(files[0]), new File(files[1]));
	}

	//////////////////////////////////////////////////////////////////////////

	public static final void copyFiles (final PrintStream out, final BufferedReader in,
									    final File sFile, final File dFile) throws IOException
	{
		out.println("Copy " + sFile + " =>" + dFile);
		if (dFile.exists())
		{
			final String	ans=getval(out, in, "overwrite " + dFile + " [y]/n");
			if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
				return;
		}

		final String	ans=getval(out, in, "use direct copy [y]/n");
		final long		cpySize, cpyStart=System.currentTimeMillis();
		if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
		{
			InputStream		iFile=null;
			OutputStream	oFile=null;
			try
			{
				iFile = new FileInputStream(sFile);
				oFile = new FileOutputStream(dFile);
				cpySize = IOCopier.copyStreams(iFile, oFile);
			}
			finally
			{
				FileUtil.closeAll(iFile, oFile);
			}
		}
		else
			cpySize = IOCopier.copyFile(sFile, dFile);
		if (cpySize < 0L)
			throw new IOException("Failed (err=" + cpySize + ") to copy");
		final long	cpyEnd=System.currentTimeMillis(), cpyDuration=cpyEnd - cpyStart;
		out.println("Copied " + cpySize + " bytes in " + cpyDuration + " msec.");
	}
	// args[0]=source file, args[1]=destination file
	public static final void copyFiles (final PrintStream out, final BufferedReader in, final String ... args) throws IOException
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; )
		{
			String	sFile=null;
			for ( ; (null == sFile) || (sFile.length() <= 0); aIndex++)
				sFile =(aIndex < numArgs) ? args[aIndex] : getval(out, in, "source file (or Quit)");
			if (isQuit(sFile)) break;

			String	dFile=null;
			for ( ; (null == dFile) || (dFile.length() <= 0); aIndex++)
				dFile =(aIndex < numArgs) ? args[aIndex] : getval(out, in, "destination file (or Quit)");
			if (isQuit(dFile)) break;

			copyFiles(out, in, new File(sFile), new File(dFile));
		}
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		try
		{
//			fileSync(System.out, getStdin(), args);
			copyFiles(System.out, getStdin(), args);
		}
		catch(Exception e)
		{
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

}
