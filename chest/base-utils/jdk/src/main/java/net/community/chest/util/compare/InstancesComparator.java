/*
 *
 */
package net.community.chest.util.compare;

import net.community.chest.lang.math.DoublesComparator;
import net.community.chest.lang.math.LongsComparator;
import net.community.chest.lang.math.NumberTables;


/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Compares 2 references without invoking the {@link Object#equals(Object)}
 * method</P>
 *
 * @param <T> The type of reference being compared
 * @author Lyor G.
 * @since Jan 6, 2009 11:01:27 AM
 */
public class InstancesComparator<T> extends AbstractComparator<T> {
    /**
     *
     */
    private static final long serialVersionUID = 95757698868272717L;
    public InstancesComparator (Class<T> valsClass) throws IllegalArgumentException
    {
        super(valsClass, false);
    }

    public static final int compareValueObjects (final Object v1, final Object v2)
    {
        if (v1 == v2)
            return 0;
        // push nulls to end
        else if (null == v1)
            return (+1);
        else if (null == v2)
            return (-1);

        // make sure same class
        {
            final Class<?>    c1=v1.getClass(), c2=v2.getClass();
            if (c1 != c2)
            {
                final String    n1=c1.getName(), n2=c2.getName();
                final int        nRes=n1.compareTo(n2);
                if (0 == nRes)    // should not be otherwise
                    throw new IllegalStateException("Same class name though not same class: " + n1 + "/" + n2);

                return nRes;
            }
        }

        // if values are comparable then compare them
        if ((v1 instanceof Comparable<?>) && (v2 instanceof Comparable<?>))
        {
            @SuppressWarnings("unchecked")
            final Comparable<Object>    c1=(Comparable<Object>) v1, c2=(Comparable<Object>) v2;
            final int                    nRes=c1.compareTo(c2);
            if (nRes != 0)
                return nRes;
        }

        final int    h1=v1.hashCode(), h2=v2.hashCode();
        if (h1 != h2)
            return (h1 - h2);

        final int    i1=System.identityHashCode(v1), i2=System.identityHashCode(v2);
        if (i1 != i2)
            return (i1 - i2);
        else    // did our best
            return (-2);
    }

    public static final int compareGeneralObjects (
            final Class<?> vc, final Object v1, final Object v2)
    {
        if ((vc != null) && Comparable.class.isAssignableFrom(vc))
        {
            final Comparable<?>    x1=(Comparable<?>) vc.cast(v1),
                                x2=(Comparable<?>) vc.cast(v2);
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final int            nRes=
                compareComparables((Comparable) x1, (Comparable) x2);
            return nRes;
        }

        final Class<?>    c1=(null == v1) ? null : v1.getClass(),
                        c2=(null == v2) ? null : v2.getClass();
        if (c1 == c2)
            return compareValueObjects(v1, v2);

        // push nulls to end
        if (null == c1)
            return (null == c2) ? 0 : (+1);
        else if (null == c2)
            return (-1);

        if (c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1))
            return compareValueObjects(v1, v2);

        // check if numbers
        final Boolean    t1=NumberTables.classifyNumberType(c1),
                        t2=NumberTables.classifyNumberType(c2);
        if ((null == t1) || (null == t2) || (t1.booleanValue() != t2.booleanValue()))
            return (-3);    // did our best

        final Number    n1=(Number) v1, n2=(Number) v2;
        if (t1.booleanValue())
            return LongsComparator.compare(n1.longValue(), n2.longValue());
        else
            return DoublesComparator.compare(n1.doubleValue(), n2.doubleValue());
    }

    public static final int compareGeneralObjects (final Object v1, final Object v2)
    {
        return compareGeneralObjects(null, v1, v2);
    }
    /*
     * @see com.emc.common.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (T v1, T v2)
    {
        return compareValueObjects(v1, v2);
    }
}
