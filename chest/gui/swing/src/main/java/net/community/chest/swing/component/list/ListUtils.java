/*
 *
 */
package net.community.chest.swing.component.list;

import java.util.Comparator;

import javax.swing.JList;
import javax.swing.ListModel;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 15, 2009 9:54:34 AM
 */
public final class ListUtils {
    private ListUtils ()
    {
        // no instance
    }
    /**
     * Attempts to locate an object in a {@link ListModel} instance
     * @param <O> Type of object being sought
     * @param lm The {@link ListModel} instance to search - if
     * <code>null</code>/empty then search returns a failure result
     * @param oc Type of object being sought - if <code>null</code> then a
     * failure result is returned
     * @param o The object to compare with - if <code>null</code> then a
     * failure result is returned
     * @param c The {@link Comparator} to use - if <code>null</code> then
     * the {@link Object#equals(Object)} call is used
     * @return The list model index - negative if no match found
     */
    public static final <O> int findElementIndex (
            final ListModel<O> lm, final Class<O> oc, final O o, final Comparator<? super O> c)
    {
        final int    numElems=(null == lm) ? 0 : lm.getSize();
        if ((numElems <= 0) || (null == oc) || (null == o))
            return (-1);

        for (int    eIndex=0; eIndex < numElems; eIndex++)
        {
            final Object    ev=lm.getElementAt(eIndex);
            if (c != null)
            {
                final Class<?>    ec=(null == ev) ? null : ev.getClass();
                if ((null == ec) || (!oc.isAssignableFrom(ec)))
                    continue;

                final int    nRes=c.compare(oc.cast(ev), o);
                if (0 == nRes)
                    return eIndex;
            }
            else
            {
                if (o.equals(ev))
                    return eIndex;
            }
        }

        return (-2);
    }

    public static final <O> int findElementIndex (
            final JList<O> l, final Class<O> oc, final O o, final Comparator<? super O> c)
    {
        return (null == l) ? (-1) : findElementIndex(l.getModel(), oc, o, c);
    }
}
