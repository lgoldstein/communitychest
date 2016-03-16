/*
 *
 */
package net.community.chest.util.compare;

import java.net.URI;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 11, 2009 9:47:47 AM
 */
public class URIComparator extends AbstractComparator<URI> {
    /**
     *
     */
    private static final long serialVersionUID = -5762296015274325969L;

    public URIComparator (boolean ascending) throws IllegalArgumentException
    {
        super(URI.class, !ascending);
    }
    /*
     * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (final URI v1, final URI v2)
    {
        final String    p1=(null == v1) ? null : v1.getScheme(),
                        p2=(null == v2) ? null : v2.getScheme();
        final int        nRes=StringUtil.compareDataStrings(p1, p2, false);
        if (nRes != 0)
            return nRes;

        final String    h1=(null == v1) ? null : v1.getPath(),
                        h2=(null == v2) ? null : v2.getPath();
        return StringUtil.compareDataStrings(h1, h2, false);
    }

    public static final URIComparator    ASCENDING=new URIComparator(true),
                                        DESCENDING=new URIComparator(false);

}
