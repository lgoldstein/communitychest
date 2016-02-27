/*
 * 
 */
package net.community.chest.io.file;

import java.io.File;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Compares the {@link File#getAbsolutePath()} values (case <U>insensitive</U>)</P>
 * 
 * @author Lyor G.
 * @since May 21, 2009 11:29:47 AM
 */
public class FilePathComparator extends AbstractFileComparator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6453061134281101743L;

	public FilePathComparator (boolean ascending)
	{
		super(!ascending);
	}
	/*
	 * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (File v1, File v2)
	{
		final String	p1=(null == v1) ? null : v1.getAbsolutePath(),
						p2=(null == v2) ? null : v2.getAbsolutePath();
		return StringUtil.compareDataStrings(p1, p2, true);
	}

	public static final FilePathComparator	ASCENDING=new FilePathComparator(true),
											DESCENDING=new FilePathComparator(false);
}
