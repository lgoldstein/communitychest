/*
 * 
 */
package net.community.chest.io.file;

import java.io.File;

import net.community.chest.lang.math.LongsComparator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 10:42:45 AM
 */
public abstract class AbstractFileDateComparator extends AbstractFileComparator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4199404448109187759L;
	protected AbstractFileDateComparator (boolean ascending)
	{
		super(!ascending);
	}

	public abstract long getFileTimestamp (final File v);
	/*
	 * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (File v1, File v2)
	{
		return LongsComparator.compare(getFileTimestamp(v1), getFileTimestamp(v2));
	}
}
