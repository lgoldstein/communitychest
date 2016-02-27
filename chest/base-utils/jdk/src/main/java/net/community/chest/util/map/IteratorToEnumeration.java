/*
 * 
 */
package net.community.chest.util.map;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Embeds an {@link Iterator} or {@link Iterable} object into an
 * {@link Enumeration} implementation</P>
 * 
 * @param <E> Type of enumerated object
 * @author Lyor G.
 * @since Mar 4, 2009 10:57:30 AM
 */
public class IteratorToEnumeration<E> implements Enumeration<E> {
	private Iterator<E>	_iter;
	public Iterator<E> getIterator ()
	{
		return _iter;
	}
	// CAVEAT EMPTOR may affect hasMoreElements/nextElement behavior if changed in mid-enumeration
	public void setIterator (Iterator<E> iter)
	{
		_iter = iter;
	}

	public IteratorToEnumeration (Iterator<E> iter)
	{
		_iter = iter;
	}

	public IteratorToEnumeration (Iterable<E> iter)
	{
		this((null == iter) ? null : iter.iterator());
	}

	public IteratorToEnumeration ()
	{
		this((Iterable<E>) null);
	}
	/*
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	@Override
	public boolean hasMoreElements ()
	{
		final Iterator<E>	i=getIterator();
		return (i != null) && i.hasNext();
	}
	/*
	 * @see java.util.Enumeration#nextElement()
	 */
	@Override
	public E nextElement ()
	{
		final Iterator<E>	i=getIterator();
		if (null == i)
			throw new NoSuchElementException("nextElement() no " + Iterator.class.getSimpleName() + " instance");

		return i.next();
	}
}
