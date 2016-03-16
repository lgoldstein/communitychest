/*
 *
 */
package net.community.chest.util.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import net.community.chest.reflect.AttributeAccessor;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>This is a {@link Collection} implementation that ignores any attempt
 * to modify it, but does not throw exceptions on such attempts</P>
 * @param <E> Type of element being collected
 * @author Lyor G.
 * @since Aug 2, 2011 1:06:56 PM
 */
public class IgnoringCollection<E> implements Collection<E> {
    public IgnoringCollection ()
    {
        super();
    }
    /*
     * @see java.util.Collection#size()
     */
    @Override
    public final int size ()
    {
        return 0;
    }
    /*
     * @see java.util.Collection#isEmpty()
     */
    @Override
    public final boolean isEmpty ()
    {
        return false;
    }
    /*
     * @see java.util.Collection#contains(java.lang.Object)
     */
    @Override
    public final boolean contains (Object o)
    {
        return false;
    }
    /*
     * @see java.util.Collection#iterator()
     */
    @Override
    public final Iterator<E> iterator ()
    {
        return Collections.emptyIterator();
    }
    /*
     * @see java.util.Collection#toArray()
     */
    @Override
    public final Object[] toArray ()
    {
        return AttributeAccessor.EMPTY_OBJECTS_ARRAY;
    }
    /*
     * @see java.util.Collection#toArray(T[])
     */
    @Override
    public final <T> T[] toArray (T[] a)
    {
        return a;
    }
    /*
     * @see java.util.Collection#add(java.lang.Object)
     */
    @Override
    public final boolean add (E e)
    {
        return false;
    }
    /*
     * @see java.util.Collection#remove(java.lang.Object)
     */
    @Override
    public final boolean remove (Object o)
    {
        return false;
    }
    /*
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    @Override
    public final boolean containsAll (Collection<?> c)
    {
        return false;
    }
    /*
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    @Override
    public final boolean addAll (Collection<? extends E> c)
    {
        return false;
    }
    /*
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    @Override
    public final boolean removeAll (Collection<?> c)
    {
        return false;
    }
    /*
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    @Override
    public final boolean retainAll (Collection<?> c)
    {
        return false;
    }
    /*
     * @see java.util.Collection#clear()
     */
    @Override
    public void clear ()
    {
        // nothing to do
    }
}
