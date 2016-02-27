/*
 * 
 */
package net.community.chest.io.file;

import java.io.File;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 10:44:42 AM
 */
public class FileLastModTimeComparator extends AbstractFileDateComparator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7734643471017093693L;

	public FileLastModTimeComparator (boolean ascending)
	{
		super(ascending);
	}
	/**
	 * @param f The {@link File} whose last-modified-time value is
	 * requested
	 * @return The file last modified time (msec.) - <B>Note(s):</B></BR>
	 * <UL>
	 * 		<LI>
	 * 		A <code>null</code> instance size is reported as
	 * 		<U>negative</U>
	 * 		</LI>
	 * 
	 * 		<LI>
	 * 		If a file does not {@link File#exists()} then its timestamp is
	 * 		reported as <U>{@link Long#MAX_VALUE}</U>
	 * 		</LI>
	 * </UL>
	 */
	public static final long getLastModifiedTimeValue (final File f)
	{
		if (null == f)
			return (-1L);
		if (!f.exists())
			return Long.MAX_VALUE;

		return f.lastModified();
	}
	/*
	 * @see net.community.chest.io.file.AbstractFileDateComparator#getFileTimestamp(java.io.File)
	 */
	@Override
	public long getFileTimestamp (File v)
	{
		return getLastModifiedTimeValue(v);
	}

	public static final FileLastModTimeComparator	ASCENDING=new FileLastModTimeComparator(true),
													DESCENDING=new FileLastModTimeComparator(false);
}
