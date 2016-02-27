package net.community.chest.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides an {@link Iterator} for <U>all</U> the {@link Boolean} values
 * (i.e., {@link Boolean#TRUE} and {@link Boolean#FALSE}. The iteration can
 * be either ascending (i.e., from <code>false</code> to <code>true</code>)
 * or descending (the other way around).</P>
 * 
 * @author Lyor G.
 * @since May 6, 2008 10:28:43 AM
 */
public final class BooleanIterator implements Iterator<Boolean> {
	private final boolean	_ascending;
	/**
	 * @return TRUE=iteration is from <code>false</code> to <code>true</code>
	 * (FALSE=the other way around).
	 */
	public final boolean isAscending ()
	{
		return _ascending;
	}

	private Boolean	_curValue	/* =null */;
	/*
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext ()
	{
		return (_curValue != null);
	}
	/*
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Boolean next ()
	{
		if (null == _curValue)
			throw new NoSuchElementException("No more elements to iterate over");

		final Boolean	ret=_curValue;
		final boolean	dir=isAscending();
		if (_curValue.booleanValue() == dir)
			_curValue = null;	// end of iteration
		else
			_curValue = Boolean.valueOf(dir);

		return ret;
	}
	/* NOTE: throws UnsupportedOperationException since N/A
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove () throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("remove() N/A");
	}
	/**
	 * Restart the iterator from 1st value
	 */
	public void restart ()
	{
		_curValue = Boolean.valueOf(!isAscending());
	}
	/**
	 * @param ascending TRUE=iteration is from <code>false</code> to
	 * <code>true</code> (FALSE=the other way around).
	 * @see #isAscending()
	 */
	public BooleanIterator (final boolean ascending)
	{
		_ascending = ascending;
		_curValue = Boolean.valueOf(!ascending);
	}
	/**
	 * Default=ascending iteration
	 * @see #isAscending()
	 * @see #BooleanIterator(boolean)
	 */
	public BooleanIterator ()
	{
		this(true);
	}
}
