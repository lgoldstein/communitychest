/*
 * 
 */
package net.community.chest.svnkit.core.wc;

import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 11:32:19 AM
 */
public abstract class AbstractSVNLocalCopyDataComparator
			extends AbstractComparator<SVNLocalCopyData> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1892172475250138651L;

	protected AbstractSVNLocalCopyDataComparator (boolean reverseMatch)
	{
		super(SVNLocalCopyData.class, reverseMatch);
	}
}
