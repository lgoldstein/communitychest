package net.community.chest.lang.math;

import net.community.chest.util.compare.AbstractComparator;


/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @param <N> Type of compared {@link Number}
 * @author Lyor G.
 * @since Oct 23, 2007 12:19:01 PM
 */
public class DefaultNumbersComparator<N extends Number & Comparable<N>>
		extends AbstractComparator<N>
		implements NumbersComparator<N> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3586033895426956778L;
	public DefaultNumbersComparator (Class<N> numClass, boolean ascending) throws IllegalArgumentException
	{
		super(numClass, !ascending);
	}
	/*
	 * @see net.community.chest.util.NumbersComparator#getNumbersClass()
	 */
	@Override
	public Class<N> getNumbersClass ()
	{
		return getValuesClass();
	}

	public static final <V extends Number & Comparable<V>> int compareNumbers (final V n1, final V n2)
	{
		if (null == n1)
			return (null == n2) ? 0 : (+1);	// push null(s) to end
		else if (null == n2)
			return (-1);	// push null(s) to end
		else
			return n1.compareTo(n2);
	}
	/*
	 * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (N o1, N o2)
	{
		return compareNumbers(o1, o2);
	}
}
