/*
 * 
 */
package net.community.chest.svnkit.core;

import net.community.chest.util.compare.AbstractComparator;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Compares using the {@link SVNErrorMessage#getErrorCode()} value(s)</P>
 * @author Lyor G.
 * @since Aug 6, 2009 2:01:16 PM
 */
public class SVNErrorMessageComparator extends AbstractComparator<SVNErrorMessage> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8897102220087260099L;

	public SVNErrorMessageComparator (boolean ascending)
	{
		super(SVNErrorMessage.class, !ascending);
	}

	public int compareErrorCodes (final SVNErrorCode c1, final SVNErrorCode	c2)
	{
		return SVNErrorCodeComparator.ASCENDING.compare(c1, c2);
	}
	/*
	 * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (SVNErrorMessage v1, SVNErrorMessage v2)
	{
		final SVNErrorCode	c1=(null == v1) ? null : v1.getErrorCode(),
							c2=(null == v2) ? null : v2.getErrorCode();
		return compareErrorCodes(c1, c2);
	}

	public static final SVNErrorMessageComparator	ASCENDING=new SVNErrorMessageComparator(true),
													DESCENDING=new SVNErrorMessageComparator(false);
}
