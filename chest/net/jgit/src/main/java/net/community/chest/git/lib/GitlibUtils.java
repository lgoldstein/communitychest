/*
 * 
 */
package net.community.chest.git.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.community.chest.io.FileUtil;
import net.community.chest.io.file.FileSeparatorStyle;
import net.community.chest.lang.SysPropsEnum;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileTreeEntry;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Tree;
import org.eclipse.jgit.lib.TreeEntry;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 15, 2011 10:57:00 AM
 */
public final class GitlibUtils {
	private GitlibUtils ()
	{
		// no instance
	}

	public static final boolean isGitControlledFolder (final File f)
	{
		if ((f == null) || (!f.isDirectory()))
			return false;

		final File	gitDir=new File(f, Constants.DOT_GIT);
		if (gitDir.exists() && gitDir.isDirectory())
			return true;

		return false;
	}
	/**
	 * The separator used internally by Git as path separator
	 */
	public static final char	GITPATH_SEPCHAR='/';
	/**
	 * @param entry The {@link TreeEntry} instance
	 * @return A {@link File} representing the absolute location of the entry
	 * in the working directory - <code>null</code> if cannot determine it
	 */
	public static final File getTreeEntryLocation (final TreeEntry entry)
	{
		final Repository	repo=(entry == null) ? null : entry.getRepository();
		final File			workDir=(repo == null) ? null : repo.getWorkDir();
		final String		name=(entry == null) ? null : entry.getFullName();
		if ((workDir == null) || (name == null) || (name.length() <= 0))
			return null;

		final String	normalized=FileSeparatorStyle.LOCAL.normalizePath(name, GITPATH_SEPCHAR);
		return new File(workDir, normalized);
	}
	/**
	 * @param tree The {@link Tree} instance in which to look for
	 * @param relativePath The <U>relative</U> path in the tree
	 * @return The matching {@link TreeEntry} - <code>null</code> if not found
	 * @throws IOException If internal access error
	 */
	public static final TreeEntry findEntry (final Tree tree, final String relativePath) throws IOException
	{
		if (tree == null)
			return null;
		if ((relativePath == null) || (relativePath.length() <= 0))
			return tree;

		TreeEntry	entry=tree.findTreeMember(relativePath);
		if (entry == null)
			entry = tree.findBlobMember(relativePath);
		return entry;
	}

	// returns number of written bytes
	public static final long dumpFile (final File outFile, final FileTreeEntry entry) throws IOException
	{
		final ObjectLoader	loader=((outFile == null) || (entry == null)) ? null : entry.openReader();
		final long			dataSize=(loader == null) ? 0L : loader.getSize();
		final byte[]		data=(dataSize <= 0L) ? null : loader.getBytes();
		if ((data == null) || (dataSize <= 0L))
			return 0L;

		final OutputStream	out=new FileOutputStream(outFile);
		try
		{
			out.write(data, 0, (int) dataSize);
		}
		finally
		{
			out.close();
		}

		return dataSize;
	}

	public static final File dumpToTempFile (final File dir, final FileTreeEntry entry) throws IOException
	{
		final String		entryName=
		    (entry == null) ? null : entry.getName(),
							entryType=
			(null == entryName) ? null : FileUtil.getExtension(entryName, true),
							entryPrefix=
			(null == entryName) ? null : FileUtil.stripExtension(entryName, entryType);
		final File			tmpFile=
			((dir == null) || (entry == null)) ? null : File.createTempFile(entryPrefix, entryType, dir);
		if (dumpFile(tmpFile, entry) <= 0L)
			return null;	// debug breakpoint

		return tmpFile;
	}
	
	public static final File dumpToTempFile (final FileTreeEntry entry) throws IOException {
		final String	tmpDir=
			(entry == null) ? null : SysPropsEnum.JAVAIOTMPDIR.getPropertyValue();
		if ((tmpDir == null) || (tmpDir.length() <= 0))
			return null;

		return dumpToTempFile(new File(tmpDir), entry);
	}
}
