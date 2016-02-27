/*
 * 
 */
package net.community.chest.io.file;

import java.io.File;

import net.community.chest.io.FileUtil;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Compares the file extension case <U>insensitive</U></P>
 * @author Lyor G.
 * @since Aug 6, 2009 12:36:20 PM
 */
public class FileTypeComparator extends AbstractFileComparator {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6095841123742338226L;

	public FileTypeComparator (boolean ascending)
	{
		super(!ascending);
	}

	public String getFileType (final File f)
	{
		return (null == f) ? null : FileUtil.getExtension(f.getAbsolutePath(), true);
	}
	/*
	 * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (File v1, File v2)
	{
		final String	e1=getFileType(v1), e2=getFileType(v2);
		return StringUtil.compareDataStrings(e1, e2, false);
	}

	public static final FileTypeComparator	ASCENDING=new FileTypeComparator(true),
											DESCENDING=new FileTypeComparator(false);
}
