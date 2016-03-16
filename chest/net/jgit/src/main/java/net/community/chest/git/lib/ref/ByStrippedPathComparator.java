/*
 *
 */
package net.community.chest.git.lib.ref;

/**
 * <P>Copyright as per GPLv2</P>
 * Examines only the last component of the reference path
 * @see RefUtils#stripRefPath(String)
 * @author Lyor G.
 * @since Mar 16, 2011 11:51:52 AM
 */
public class ByStrippedPathComparator extends AbstractRefComparator {
    /**
     *
     */
    private static final long serialVersionUID = -4762020933562535904L;

    public ByStrippedPathComparator (boolean ascending)
    {
        super(RefAttributeType.NAME, ascending);
    }
    /*
     * @see net.community.chest.git.lib.ref.AbstractRefComparator#compareAttributeValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareAttributeValues (Object v1, Object v2) throws UnsupportedOperationException
    {
        final String    n1=RefUtils.stripRefPath((v1 == null) ? null : v1.toString()),
                        n2=RefUtils.stripRefPath((v2 == null) ? null : v2.toString());
        return super.compareAttributeValues(n1, n2);
    }

    public static final ByStrippedPathComparator    ASCENDING=new ByStrippedPathComparator(true),
                                                    DESCENDING=new ByStrippedPathComparator(false);
}
