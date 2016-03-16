/*
 *
 */
package net.community.chest.git.lib.ref;

import org.eclipse.jgit.lib.Ref;

import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 15, 2011 8:45:46 AM
 *
 */
public abstract class AbstractRefComparator extends AbstractComparator<Ref> {
    /**
     *
     */
    private static final long serialVersionUID = -340800001051885746L;
    private final RefAttributeType    _attrType;
    /**
     * @return The {@link Ref} attribute being compared
     */
    public final RefAttributeType getAttributeType ()
    {
        return _attrType;
    }

    protected AbstractRefComparator (RefAttributeType attrType, boolean ascending)
    {
        super(Ref.class, !ascending);

        if ((_attrType=attrType) == null)
            throw new IllegalStateException("No attribute type specified");
    }
    /**
     * A default implementation that checks if both values are {@link Comparable}
     * and if so then compares them
     * @param v1 1st {@link Ref} attribute value to compare
     * @param v2 2nd {@link Ref} attribute value to compare
     * @return negative if <code>v1 < v2</code>, positive if <code>v1 > v2</code>
     * and zero if equal
     * @throws UnsupportedOperationException if both values are non-<code>null</code>
     * but are not {@link Comparable}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public int compareAttributeValues (Object v1, Object v2) throws UnsupportedOperationException
    {
        if (v1 == null)
            return (v2 == null) ? 0 : (+1);    // push null(s) to end
        else if (v2 == null)
            return (-1);    // push null(s) to end

        if ((v1 instanceof Comparable<?>) && (v2 instanceof Comparable<?>))
            return ((Comparable) v1).compareTo(v2);

        throw new UnsupportedOperationException("compareAttributeValues(" + getAttributeType() + ")"
                                              + "[" + v1 + "/" + v2 + "] N/A");
    }
    /*
     * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (Ref v1, Ref v2)
    {
        final RefAttributeType    attrType=getAttributeType();
        final Object            o1=(v1 == null) ? null : attrType.getAttributeValue(v1),
                                o2=(v2 == null) ? null : attrType.getAttributeValue(v2);
        return compareAttributeValues(o1, o2);
    }
}
