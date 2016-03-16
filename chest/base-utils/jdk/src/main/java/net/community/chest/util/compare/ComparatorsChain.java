package net.community.chest.util.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Applies a "chain" of {@link Comparator}-s in the order in which they appear
 * in the {@link Collection} - stopping at first one that returns non-zero
 * value. Useful for secondary/tertiary/etc. sorting</P>
 *
 * @param <C> The {@link Comparator} superclass
 * @author Lyor G.
 * @since Dec 2, 2007 8:37:22 AM
 */
public class ComparatorsChain<C> extends ArrayList<Comparator<? super C>> implements Comparator<C> {
    /**
     *
     */
    private static final long serialVersionUID = 5279110367093884920L;
    public ComparatorsChain ()
    {
        super();
    }

    public ComparatorsChain (Collection<? extends Comparator<? super C>> c)
    {
        super(c);
    }

    public ComparatorsChain (int initialCapacity)
    {
        super(initialCapacity);
    }

    public static final <V> int compare (V o1, V o2, Collection<Comparator<? super V>> cl)
    {
        if ((null == cl) || (cl.size() <= 0))
        {
            // prefer non-null first
            if (null == o1)
                return (null == o2) ? 0 : (+1);
            else if (null == o2)
                return (-1);

            if (o1 instanceof Comparable<?>)
            {
                @SuppressWarnings("unchecked")
                final Comparable<Object> oc=(Comparable<Object>) o1;
                return oc.compareTo(o2);
            }

            if (o1.equals(o2))
                return 0;

            return (o1.hashCode() - o2.hashCode());
        }

        for (final Comparator<? super V> c : cl)
        {
            final int    nRes=c.compare(o1, o2);
            if (nRes != 0)
                return nRes;
        }

        return 0;
    }

    @SafeVarargs
    public static final <V> int compare (V o1, V o2, Comparator<? super V> ... cl)
    {
        return compare(o1, o2, ((null == cl) || (cl.length <= 0)) ? null : SetsUtils.uniqueSetOf(cl));
    }
    /*
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare (C o1, C o2)
    {
        return compare(o1, o2, this);
    }
}
