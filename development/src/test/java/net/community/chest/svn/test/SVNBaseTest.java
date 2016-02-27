/*
 * 
 */
package net.community.chest.svn.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.util.Set;
import java.util.TreeSet;

import net.community.chest.Triplet;
import net.community.chest.io.IOCopier;
import net.community.chest.io.file.FileIOUtils;
import net.community.chest.lang.StringUtil;
import net.community.chest.svnkit.SVNFoldersFilter;
import net.community.chest.test.TestBase;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCClient;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 9:28:47 AM
 */
public class SVNBaseTest extends TestBase {
	public SVNBaseTest ()
	{
		super();
	}

	//////////////////////////////////////////////////////////////////////////

	public static final void testSVNFoldersFilter (
			final PrintStream out, final File rootFolder)
	{
		if (rootFolder.isFile())
		{
			out.append('\t').append(rootFolder.getAbsolutePath()).println();
			return;
		}

		out.append("Scanning ").append(rootFolder.getAbsolutePath()).println();

		final File[]	subFiles=rootFolder.listFiles((FileFilter) SVNFoldersFilter.DEFAULT);
		final int		numSubFiles=(null == subFiles) ? 0 : subFiles.length;
		if (numSubFiles <= 0)
			return;

		for (final File f : subFiles)
			testSVNFoldersFilter(out, f);
	}

	/* -------------------------------------------------------------------- */

	// args[i]=root folder to be checked
	public static final void testSVNFoldersFilter (
			final BufferedReader in, final PrintStream out, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	path=
				(aIndex < numArgs) ? args[aIndex] : getval(out, in, "file path (or Quit)");
			if ((null == path) || (path.length() <= 0))
				continue;
			if (isQuit(path)) break;

			for ( ; ; )
			{
				testSVNFoldersFilter(out, new File(path));
				final String	ans=getval(out, in, "again [y]/n");
				if ((ans != null) && (ans.length() > 0) && ('y' != Character.toLowerCase(ans.charAt(0))))
					break;
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////

	public static final void testSVNFoldersSync (
			final PrintStream out, final SVNWCClient wcc,
			final File	srcFile, final File dstFile)
		throws IOException, SVNException
	{
		final String	srcPath=srcFile.getAbsolutePath(),
						dstPath=dstFile.getAbsolutePath();
		if (0 == StringUtil.compareDataStrings(srcPath, dstPath, false))
			throw new UnsupportedOperationException("Cannot sync file with itself");
			
		if (srcFile.isFile() != dstFile.isFile())
			throw new UnsupportedOperationException("Cannot sync folder with file");

		if (srcFile.isFile())
		{
			final Triplet<Long,Byte,Byte>	diff=FileIOUtils.findDifference(srcFile, dstFile);
			if (null == diff)
				return;

			final long	cpySize=IOCopier.copyFile(srcFile, dstFile);
			if (cpySize < 0L)
				throw new StreamCorruptedException("Copy error (" + cpySize + ")"
												 + " on copying " + srcPath
												 + " to " + dstPath);
			out.append("Updated contents of ").append(dstPath).println();
			return;
		}

		final File[]		srcFiles=srcFile.listFiles((FileFilter) SVNSyncFilesFilter.DEFAULT);
		final int			numSources=(null == srcFiles) ? 0 : srcFiles.length;
		final Set<String>	srcProcd=new TreeSet<String>(), dstProcd=new TreeSet<String>();
		for (int sIndex=0; sIndex < numSources; sIndex++)
		{
			final File		f=srcFiles[sIndex];
			final String	n=f.getName();
			srcProcd.add(n);

			final File		d=new File(dstFile, n);
			if (d.exists())
			{
				testSVNFoldersSync(out, wcc, f, d);
			}
			else if (f.isDirectory())
			{
				wcc.doAdd(d, true, true, true, SVNDepth.INFINITY, false, false, true);

				out.append("Created new folder: ").append(d.getAbsolutePath()).println();
				testSVNFoldersSync(out, wcc, f, d);
			}
			else if (f.isFile())
			{
				final long	cpySize=IOCopier.copyFile(f, d);
				if (cpySize < 0L)
					throw new StreamCorruptedException("Copy error (" + cpySize + ")"
													 + " on copying " + f
													 + " to " + d);
				wcc.doAdd(d, false, false, true, SVNDepth.EMPTY, false, false, true);
				out.append("Created contents of ").append(d.getAbsolutePath()).println();
			}
			else
				throw new IOException("Unknown non-existing source file: " + f);

			dstProcd.add(n);
		}

		final File[]	dstFiles=dstFile.listFiles((FileFilter) SVNSyncFilesFilter.DEFAULT);
		if ((null == dstFiles) || (dstFiles.length <= 0))
			return;

		for (final File d : dstFiles)
		{
			final String	n=d.getName();
			if (dstProcd.contains(n))
				continue;

			wcc.doDelete(d, true, true, false);
			out.append("Deleted ").append(d.getAbsolutePath()).println();
		}
	}

	/* -------------------------------------------------------------------- */

	public static final void testSVNFoldersSync (
			final BufferedReader in, final PrintStream out, final String ... args)
	{
		final String			username=null, password=null;
		final SVNClientManager	mgr=
			SVNClientManager.newInstance(new DefaultSVNOptions(), new DefaultSVNAuthenticationManager(null, true, username, password));
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; )
		{
			String	srcFolder=null;
			for ( ; (null == srcFolder) || (srcFolder.length() <= 0) ; aIndex++)
				srcFolder = (aIndex < numArgs) ? args[aIndex] : getval(out, in, "source folder (or Quit)");
			if (isQuit(srcFolder)) break;

			String	dstFolder=null;
			for ( ; (null == dstFolder) || (dstFolder.length() <= 0) ; aIndex++)
				dstFolder = (aIndex < numArgs) ? args[aIndex] : getval(out, in, "destination folder (or Quit)");
			if (isQuit(dstFolder)) continue;

			try
			{
				testSVNFoldersSync(out, mgr.getWCClient(), new File(srcFolder), new File(dstFolder));
			}
			catch(Exception ioe)
			{
				System.err.append(ioe.getClass().getName())
						  .append(" while sync src=").append(srcFolder)
						  .append(" with dst=").append(dstFolder)
						  .append(": ").append(ioe.getMessage())
						 .println();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
//		testSVNFoldersFilter(getStdin(), System.out, args);
		testSVNFoldersSync(getStdin(), System.out, args);
	}
}
