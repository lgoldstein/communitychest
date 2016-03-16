/*
 *
 */
package net.community.chest.svnkit.core;

import net.community.chest.util.compare.AbstractComparator;

import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Compares using the {@link SVNException#getErrorMessage()} values</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 1:59:44 PM
 */
public class SVNExceptionComparator extends AbstractComparator<SVNException> {
    /**
     *
     */
    private static final long serialVersionUID = 8548541154493374757L;

    public SVNExceptionComparator (boolean ascending)
    {
        super(SVNException.class, !ascending);
    }

    public int compareMessages (final SVNErrorMessage m1, final SVNErrorMessage    m2)
    {
        return SVNErrorMessageComparator.ASCENDING.compareValues(m1, m2);
    }
    /*
     * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (SVNException v1, SVNException v2)
    {
        final SVNErrorMessage    m1=(null == v1) ? null : v1.getErrorMessage(),
                                m2=(null == v2) ? null : v2.getErrorMessage();
        return compareMessages(m1, m2);
    }

    public static final SVNExceptionComparator    ASCENDING=new SVNExceptionComparator(true),
                                                DESCENDING=new SVNExceptionComparator(false);
}
