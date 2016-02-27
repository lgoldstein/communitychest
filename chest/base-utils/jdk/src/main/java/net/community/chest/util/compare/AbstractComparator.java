/*
 * 
 */
package net.community.chest.util.compare;

import java.io.Serializable;
import java.util.Comparator;

import net.community.chest.BaseTypedValuesContainer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides a &quot;wrapper&quot; for the {@link Comparator} interface
 * by also providing an automatic &quot;reversal&quot; of the comparison
 * result</P>
 * @param <T> The compared type
 * @author Lyor G.
 * @since Sep 22, 2008 9:03:54 AM
 */
public abstract class AbstractComparator<T> extends BaseTypedValuesContainer<T>
			implements Comparator<T>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5377741142565660830L;
	private final boolean	_reverseMatch;
	/**
	 * @return TRUE=iteration is from <code>false</code> to <code>true</code>
	 * (FALSE=the other way around).
	 */
	public final boolean isReverseMatch ()
	{
		return _reverseMatch;
	}

	protected AbstractComparator (final Class<T> valsClass, final boolean reverseMatch) throws IllegalArgumentException
	{
		super(valsClass);
		_reverseMatch = reverseMatch;
	}

	public abstract int compareValues (final T v1, final T v2);
	/*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare (T o1, T o2)
	{
		final int	nRes=compareValues(o1, o2);
		if (0 == nRes)
			return 0;

		if (isReverseMatch())
			return (0 - nRes);

		return nRes;
	}
	/**
	 * Checks if a {@link Comparable} value is within a specified range. The
	 * range can be <U>open-ended</U> - i.e., if range start/end is null then
	 * it is considered to be negative/positive infinity respectively.
	 * @param <C> The {@link Comparable} generic type
	 * @param v Value to be considered - if null, then considered <U>not</U>
	 * to be in the range (even if open-ended)
	 * @param sVal Start value - candidate value must be greater or equal to
	 * it (null == negative infinity)
	 * @param eVal End value - candidate value must be less or equal to
	 * it (null == positive infinity)
	 * @return If (non-null) value within specified (open-ended) range
	 */
	public static final <C extends Comparable<C>> boolean valueInRange (final C v, final C sVal, final C eVal)
	{
		if (null == v)
			return false;
	
		if ((sVal != null) && (sVal.compareTo(v) > 0))
			return false;
	
		if ((eVal != null) && (eVal.compareTo(v) < 0))
			return false;
	
		return true;
	}

	public static final <C extends Comparable<C>> int compareComparables (final C o1, final C o2)
	{
		if (null == o1)
			return (null == o2) ? 0 : (+1);	// push null(s) to end
		else if (null == o2)
			return (-1);	// push null(s) to end
		return o1.compareTo(o2);
	}
	/**
	 * @param o1 first {@link Object} to compare
	 * @param o2 second {@link Object} to compare
	 * @return TRUE if both null or <code>o1.equals(o2)</code>
	 */
	public static final boolean compareObjects (final Object o1, final Object o2)
	{
		if (o1 == o2)
			return true;
		else if ((null == o1) || (null == o2))
			return false;	// they can't be both null since o1 == o2 would have fired
		else
			return o1.equals(o2);
	}
}
