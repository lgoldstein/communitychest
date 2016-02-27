/*
 * 
 */
package net.community.chest.svnkit.core;

import net.community.chest.util.compare.AbstractComparator;

import org.tmatesoft.svn.core.SVNErrorCode;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Compares the {@link SVNErrorCode#getCode()} values</P>
 * 
 * @author Lyor G.
 * @since Aug 6, 2009 1:55:47 PM
 */
public class SVNErrorCodeComparator extends AbstractComparator<SVNErrorCode> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4607364318128190718L;

	public SVNErrorCodeComparator (boolean ascending)
	{
		super(SVNErrorCode.class, !ascending);
	}
	/*
	 * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (SVNErrorCode v1, SVNErrorCode v2)
	{
		final int	c1=(null == v1) ? 0 : v1.getCode(),
					c2=(null == v2) ? 0 : v2.getCode();
		return (c1 - c2);
	}

	public static final SVNErrorCodeComparator	ASCENDING=new SVNErrorCodeComparator(true),
												DESCENDING=new SVNErrorCodeComparator(false);
}
