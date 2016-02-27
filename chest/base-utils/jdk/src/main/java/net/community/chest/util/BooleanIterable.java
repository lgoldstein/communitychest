package net.community.chest.util;

import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides an {@link Iterable} implementation over <U>all</U> the
 * {@link Boolean} values. The iteration can be either ascending (i.e.,
 * from <code>false</code> to <code>true</code>) or descending (the other
 * way around).</P>
 * 
 * @author Lyor G.
 * @since May 6, 2008 10:31:31 AM
 */
public final class BooleanIterable implements Iterable<Boolean> {
	private final boolean	_ascending;
	/**
	 * @return TRUE=iteration is from <code>false</code> to <code>true</code>
	 * (FALSE=the other way around).
	 */
	public final boolean isAscending ()
	{
		return _ascending;
	}

	private BooleanIterable (final boolean ascending)
	{
		_ascending = ascending;
	}
	/*
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	@CoVariantReturn
	public BooleanIterator iterator ()
	{
		return new BooleanIterator(isAscending());
	}

	public static final BooleanIterable	ASCENDING=new BooleanIterable(true),
										DESCENDING=new BooleanIterable(false);
}
