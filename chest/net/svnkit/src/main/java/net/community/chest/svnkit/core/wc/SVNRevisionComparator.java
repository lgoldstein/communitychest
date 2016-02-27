/*
 * 
 */
package net.community.chest.svnkit.core.wc;

import net.community.chest.lang.math.LongsComparator;
import net.community.chest.util.compare.AbstractComparator;

import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 11, 2009 12:23:37 PM
 */
public class SVNRevisionComparator extends AbstractComparator<SVNRevision> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2632184247834143815L;

	public SVNRevisionComparator (boolean ascending)
	{
		super(SVNRevision.class, !ascending);
	}

	public static final int compareRevisions (SVNRevision r1, SVNRevision r2)
	{
		final long	v1=(null == r1) ? (-1L) : r1.getNumber(),
					v2=(null == r2) ? (-1L) : r2.getNumber();
		// prefer versioned before un-versioned
		if (v1 < 0L)
			return (v1 < 0L) ? 0 : (+1);
		else if (v2 < 0L)
			return (-1);

		return LongsComparator.compare(v1, v2);
	}
	/*
	 * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (SVNRevision v1, SVNRevision v2)
	{
		return compareRevisions(v1, v2);
	}

	public static final SVNRevisionComparator	ASCENDING=new SVNRevisionComparator(true),
												DESCENDING=new SVNRevisionComparator(false);
}
