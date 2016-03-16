/*
 *
 */
package net.community.chest.io.url;

import java.net.URL;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 3, 2011 1:00:49 PM
 */
public class ByPathURLComparator extends AbstractURLComparator {
    /**
     *
     */
    private static final long serialVersionUID = -281303759185372630L;

    public ByPathURLComparator (boolean ascending) throws IllegalArgumentException
    {
        super(ascending);
    }
    /*
     * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (final URL v1, final URL v2)
    {
        final String    h1=adjustPathValue((null == v1) ? null : v1.getPath()),
                        h2=adjustPathValue((null == v2) ? null : v2.getPath());
        return StringUtil.compareDataStrings(h1, h2, false);
    }

    public static final ByPathURLComparator    BY_PATH_ASCENDING=new ByPathURLComparator(true),
                                            BY_PATH_DESCENDING=new ByPathURLComparator(false);
}
