/*
 *
 */
package net.community.chest.svnkit.core;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.compare.AbstractComparator;

import org.tmatesoft.svn.core.SVNURL;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 1:48:24 PM
 */
public class SVNURLComparator extends AbstractComparator<SVNURL> {
    /**
     *
     */
    private static final long serialVersionUID = -2085541383876299010L;

    public SVNURLComparator (boolean ascending)
    {
        super(SVNURL.class, !ascending);
    }

    public static final int compareURLs (SVNURL v1, SVNURL v2)
    {
        final String    s1=(null == v1) ? null : v1.toString(),
                        s2=(null == v2) ? null : v2.toString();
        return StringUtil.compareDataStrings(s1, s2, false);
    }
    /*
     * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (SVNURL v1, SVNURL v2)
    {
        return compareURLs(v1, v2);
    }

    public static final SVNURLComparator    ASCENDING=new SVNURLComparator(true),
                                            DESCENDING=new SVNURLComparator(false);
}
