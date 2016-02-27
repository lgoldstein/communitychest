/*
 * 
 */
package net.community.chest.svnkit.core.wc;

import net.community.chest.io.file.FileNameComparator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 11:37:01 AM
 */
public class SVNLocalFileNameComparator extends AbstractSVNLocalFileComparator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3129720015737684724L;

	public SVNLocalFileNameComparator (boolean ascending)
	{
		super(ascending ? FileNameComparator.ASCENDING : FileNameComparator.DESCENDING, false);
	}

	public static final SVNLocalFileNameComparator	ASCENDING=new SVNLocalFileNameComparator(true),
													DESCENDING=new SVNLocalFileNameComparator(false);
}
