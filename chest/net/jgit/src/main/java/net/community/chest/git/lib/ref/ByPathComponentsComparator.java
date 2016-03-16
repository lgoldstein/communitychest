/*
 *
 */
package net.community.chest.git.lib.ref;

import java.util.List;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 16, 2011 12:25:07 PM
 */
public class ByPathComponentsComparator extends AbstractRefComparator {
    /**
     *
     */
    private static final long serialVersionUID = -6488166158336320853L;

    public ByPathComponentsComparator (boolean ascending)
    {
        super(RefAttributeType.NAME, ascending);
    }
    /*
     * @see net.community.chest.git.lib.ref.AbstractRefComparator#compareAttributeValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareAttributeValues (Object v1, Object v2) throws UnsupportedOperationException
    {
        final List<String>    c1=RefUtils.getRefPathComponents((v1 == null) ? null : v1.toString()),
                            c2=RefUtils.getRefPathComponents((v2 == null) ? null : v2.toString());
        final int            l1=(c1 == null) ? 0 : c1.size(),
                            l2=(c2 == null) ? 0 : c2.size(),
                            cLen=Math.min(l1, l2);
        for (int    cIndex=0; cIndex < cLen; cIndex++)
        {
            final String    n1=c1.get(cIndex), n2=c2.get(cIndex);
            final int        nRes=super.compareAttributeValues(n1, n2);
            if (nRes != 0)
                return nRes;
        }

        // if common components are the same, then shorter one wins
        return (l1 - l2);
    }

    public static final ByPathComponentsComparator    ASCENDING=new ByPathComponentsComparator(true),
                                                    DESCENDING=new ByPathComponentsComparator(false);
}
