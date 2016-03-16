/*
 *
 */
package net.community.chest.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Wraps an {@link Iterable} or {@link Iterator} instance into an {@link Enumeration}</P>
 * @param <V> Type of parameter being enumerated
 * @author Lyor G.
 * @since Nov 14, 2010 1:55:03 PM
 */
public class IterableEnumeration<V> implements Enumeration<V> {
    private Iterator<V>    _iterator;
    public Iterator<V> getIterator ()
    {
        return _iterator;
    }

    public void setIterator (Iterator<V> iterator)
    {
        _iterator = iterator;
    }

    public IterableEnumeration (Iterator<V> iterator)
    {
        _iterator = iterator;
    }

    public IterableEnumeration (Iterable<V> iterable)
    {
        this((iterable == null) ? null : iterable.iterator());
    }

    public IterableEnumeration ()
    {
        this((Iterator<V>) null);
    }
    /*
     * @see java.util.Enumeration#hasMoreElements()
     */
    @Override
    public boolean hasMoreElements ()
    {
        final Iterator<?>    iter=getIterator();
        if ((iter != null) && iter.hasNext())
            return true;

        return false;
    }
    /*
     * @see java.util.Enumeration#nextElement()
     */
    @Override
    public V nextElement ()
    {
        final Iterator<? extends V>    iter=getIterator();
        if (iter == null)
            throw new NoSuchElementException("No iterator");

        return iter.next();
    }
}
