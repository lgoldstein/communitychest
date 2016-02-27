/*
 * 
 */
package net.community.chest.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <P>Copyright 2011 as per GPLv2</P>
 * 
 * Provides queue-like behavior for a {@link Set} of objects
 * @param <E> Type of element being queued
 * @author Lyor G.
 * @since Oct 10, 2011 12:29:40 PM
 */
public class BlockingSet<E> extends AbstractQueue<E> implements BlockingQueue<E> {
    /** Main lock guarding all access */
    private final ReentrantLock _lock;

    /** The {@link Condition} for waiting takes */
    private final Condition _notEmpty;

    /** The backing {@link Set} of elements */
    private final Set<E>	_backingSet; 

    public BlockingSet ()
    {
    	this(false);
    }

    public BlockingSet (boolean fair)
	{
    	this(fair, null);
	}

    protected BlockingSet (Set<E> backingSet)
    {
    	this(false, backingSet);
    }

    protected BlockingSet (boolean fair, Set<E> backingSet)
    {
        _lock = new ReentrantLock(fair);
        _notEmpty = _lock.newCondition();
        _backingSet = (backingSet == null) ? (fair ? new HashSet<E>() : new LinkedHashSet<E>()) : backingSet;
    }

    @Override
	public boolean offer (E e)
    {
        if (e == null)
        	throw new NullPointerException("Null item offered");

        final ReentrantLock lock=this._lock;
        lock.lock();
        try
        {
        	if (_backingSet.add(e))
        		_notEmpty.signal();

        	return true;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
	public boolean contains (Object o)
    {
    	if (o == null)
    		return false;

    	final ReentrantLock lock=this._lock;
        lock.lock();
        try
        {
        	return _backingSet.contains(o);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
	public boolean containsAll (Collection<?> c)
    {
    	final ReentrantLock lock=this._lock;
        lock.lock();
        try
        {
        	return _backingSet.containsAll(c);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
	public E poll ()
    {
        final ReentrantLock lock=this._lock;
        lock.lock();
        try
        {
            return extract();
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
	public E poll (long timeout, TimeUnit unit) throws InterruptedException
    {
        final ReentrantLock lock=this._lock;
    	long 				nanos=unit.toNanos(timeout);

    	lock.lockInterruptibly();
        try
        {
            for ( ; ; )
            {
            	final E x=extract();
            	if (x != null)
                    return x;

            	if (nanos <= 0)
                    return null;

                try
                {
                    nanos = _notEmpty.awaitNanos(nanos);
                }
                catch (InterruptedException ie)
                {
                    _notEmpty.signal(); // propagate to non-interrupted thread
                    throw ie;
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    /**
     * Waits infinitely (or until interrupted) for an entry to become
     * available
     * @return The removed entry
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
	public E take () throws InterruptedException
    {
    	final ReentrantLock lock=this._lock;
    	lock.lockInterruptibly();
    	try
    	{
    		try
    		{
                while (_backingSet.size() <= 0)
                    _notEmpty.await();
            }
    		catch (InterruptedException ie)
    		{
    			_notEmpty.signal(); // propagate to non-interrupted thread
                throw ie;
            }

    		return extract();
        }
    	finally
    	{
            lock.unlock();
        }
    }

    @Override
	public int size ()
    {
        final ReentrantLock lock = this._lock;
        lock.lock();
        try
        {
            return _backingSet.size();
        }
        finally
        {
            lock.unlock();
        }
    }


	@Override
	public boolean remove (Object o)
	{
        final ReentrantLock lock = this._lock;
        lock.lock();
        try
        {
        	return _backingSet.remove(o);
        }
        finally
        {
            lock.unlock();
        }
	}

    @Override
	public void put (E e) throws InterruptedException
	{
		if (!offer(e))
            throw new IllegalStateException("Queue full");
	}

	@Override
	public E remove ()
	{
        final E x=poll();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException("Nothing to poll");
	}

	@Override
	public E element ()
	{
        final E x=peek();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException("Nothing to peek");
	}

	@Override
	public E peek ()
	{
        final ReentrantLock lock = this._lock;
        lock.lock();
        try
        {
            return peekFirstElement();
        }
        finally
        {
            lock.unlock();
        }
	}

	@Override
	public Iterator<E> iterator ()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray ()
	{
        final ReentrantLock lock=this._lock;
        lock.lock();
        try
        {
        	return _backingSet.toArray();
        }
        finally
        {
            lock.unlock();
        }
    }

	@Override
	public <T> T[] toArray (T[] a)
	{
        final ReentrantLock lock=this._lock;
        lock.lock();
        try
        {
        	return _backingSet.toArray(a);
        }
        finally
        {
            lock.unlock();
        }
	}

	@Override
	public void clear ()
	{
        final ReentrantLock lock=this._lock;
        lock.lock();
		try
		{
			_backingSet.clear();
		}
        finally
        {
            lock.unlock();
        }
	}

	@Override
	public boolean offer (E e, long timeout, TimeUnit unit) throws InterruptedException
	{
		if ((unit == null) || (timeout <= 0L))
			throw new IllegalArgumentException("Bad time specification: " + timeout + " " + unit);
		return offer(e);
	}

	@Override
	public int remainingCapacity ()
	{
		return Integer.MAX_VALUE - size();
	}

	@Override
	public int drainTo (Collection<? super E> c)
	{
        return drainTo(c, Integer.MAX_VALUE);
	}

	@Override
	public int drainTo (Collection<? super E> c, int maxElements)
	{
		if ((c == null) || (maxElements < 0))
			throw new IllegalArgumentException("Bad arguments: " + maxElements + "/" + (c == null));

		final ReentrantLock lock=this._lock;
        lock.lock();
        try
        {
            final int n=Math.min(maxElements, _backingSet.size());
            for (int index=0; index < n; index++)
            	c.add(extract());
            return n;
        }
        finally
        {
            lock.unlock();
        }
	}

	protected E extract ()
    {
		final E	x=peekFirstElement();
		if (x == null)
			return null;

		if (!_backingSet.remove(x))
    		throw new IllegalStateException("Failed to remove element=" + x);
    	return x;
    }

	protected E peekFirstElement ()
	{
    	if (_backingSet.isEmpty())
    		return null;
 
    	final Iterator<? extends E>	i=_backingSet.iterator();
    	return i.next();
	}

    @Override
	public String toString ()
    {
        final ReentrantLock lock = this._lock;
        lock.lock();
        try
        {
            return _backingSet.toString();
        }
        finally
        {
            lock.unlock();
        }
    }
}
