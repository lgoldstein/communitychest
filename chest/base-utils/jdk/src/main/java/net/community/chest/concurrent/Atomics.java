/*
 * 
 */
package net.community.chest.concurrent;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Useful methods for handling atomic integer/long/etc.</P>
 * @author Lyor G.
 * @since Sep 14, 2008 9:03:48 AM
 */
public final class Atomics {
	private Atomics ()
	{
		// do nothing
	}
	/**
	 * Fills array elements range with specified value - <B>Note:</B> if bad
	 * range specified then best effort is done (which could amount to nothing)
	 * @param a array to be filled - if null/empty then nothing is done
	 * @param startPos start element index (inclusive)
	 * @param len number of elements to be set
	 * @param val value to be used for filling
	 * @return same as input instance
	 */
	public static final AtomicIntegerArray fillArray (final AtomicIntegerArray 	a,
													  final int					startPos,
													  final int					len,
													  final int 				val)
	{
		if ((null == a) || (startPos < 0) || (len <= 0))
			return a;

		final int	maxPos=Math.min(startPos + len, a.length());
		for (int	aIndex=startPos; aIndex < maxPos; aIndex++)
			a.set(aIndex, val);

		return a;
	}
	/**
	 * Fills all array elements with specified value
	 * @param a array to be filled - if null/empty then nothing is done
	 * @param val value to be used for filling
	 * @return same as input instance
	 */
	public static final AtomicIntegerArray fillArray (final AtomicIntegerArray a, final int val)
	{
		return (null == a) ? a : fillArray(a, 0, a.length(), val);
	}
	/**
	 * Fills array elements range with specified value - <B>Note:</B> if bad
	 * range specified then best effort is done (which could amount to nothing)
	 * @param a array to be filled - if null/empty then nothing is done
	 * @param startPos start element index (inclusive)
	 * @param len number of elements to be set
	 * @param val value to be used for filling
	 * @return same as input instance
	 */
	public static final AtomicLongArray fillArray (final AtomicLongArray	a,
												   final int				startPos,
												   final int				len,
												   final long 				val)
	{
		if ((null == a) || (startPos < 0) || (len <= 0))
			return a;

		final int	maxPos=Math.min(startPos + len, a.length());
		for (int	aIndex=startPos; aIndex < maxPos; aIndex++)
			a.set(aIndex, val);

		return a;
	}
	/**
	 * Fills all array elements with specified value
	 * @param a array to be filled - if null/empty then nothing is done
	 * @param val value to be used for filling
	 * @return same as input instance
	 */
	public static final AtomicLongArray fillArray (final AtomicLongArray a, final long val)
	{
		return (null == a) ? a : fillArray(a, 0, a.length(), val);
	}
	/**
	 * Fills array elements range with specified value - <B>Note:</B> if bad
	 * range specified then best effort is done (which could amount to nothing)
	 * @param <E> The type of reference in the array
	 * @param a array to be filled - if null/empty then nothing is done
	 * @param startPos start element index (inclusive)
	 * @param len number of elements to be set
	 * @param val value to be used for filling
	 * @return same as input instance
	 */
	public static final <E> AtomicReferenceArray<E> fillArray (final AtomicReferenceArray<E> 	a,
															   final int					 	startPos,
															   final int						len,
															   final E							val)
	{
		if ((null == a) || (startPos < 0) || (len <= 0))
			return a;

		final int	maxPos=Math.min(startPos + len, a.length());
		for (int	aIndex=startPos; aIndex < maxPos; aIndex++)
			a.set(aIndex, val);

		return a;
	}
	/**
	 * Fills all array elements with specified value
	 * @param <E> The type of reference in the array
	 * @param a array to be filled - if null/empty then nothing is done
	 * @param val value to be used for filling
	 * @return same as input instance
	 */
	public static final <E> AtomicReferenceArray<E> fillArray (final AtomicReferenceArray<E> a, final E val)
	{
		return (null == a) ? a : fillArray(a, 0, a.length(), val);
	}
	/**
	 * Zero-es elements in range
	 * @param a array to be "cleared" - if null/empty then nothing is done
	 * @param startPos start element index (inclusive)
	 * @param len number of elements to be set
	 * @return same as input instance
	 */
	public static final AtomicIntegerArray clearArray (final AtomicIntegerArray a, final int startPos, final int len)
	{
		return fillArray(a, startPos, len, 0);
	}
	/**
	 * Zero-es all elements
	 * @param a array to be "cleared" - if null/empty then nothing is done
	 * @return same as input instance
	 */
	public static final AtomicIntegerArray clearArray (final AtomicIntegerArray a)
	{
		return (null == a) ? a : clearArray(a, 0, a.length());
	}
	/**
	 * Zero-es elements in range
	 * @param a array to be "cleared" - if null/empty then nothing is done
	 * @param startPos start element index (inclusive)
	 * @param len number of elements to be set
	 * @return same as input instance
	 */
	public static final AtomicLongArray clearArray (final AtomicLongArray a, final int startPos, final int len)
	{
		return fillArray(a, startPos, len, 0L);
	}
	/**
	 * Zero-es all elements
	 * @param a array to be "cleared" - if null/empty then nothing is done
	 * @return same as input instance
	 */
	public static final AtomicLongArray clearArray (final AtomicLongArray a)
	{
		return (null == a) ? a : clearArray(a, 0, a.length());
	}
	/**
	 * Null-ifies elements in range
	 * @param <E> The type of reference in the array
	 * @param a array to be "cleared" - if null/empty then nothing is done
	 * @param startPos start element index (inclusive)
	 * @param len number of elements to be set
	 * @return same as input instance
	 */
	public static final <E> AtomicReferenceArray<E> clearArray (final AtomicReferenceArray<E> a, final int startPos, final int len)
	{
		return (null == a) ? null : fillArray(a, startPos, len, null);
	}
	/**
	 * Null-ifies all elements
	 * @param <E> The type of reference in the array
	 * @param a array to be "cleared" - if null/empty then nothing is done
	 * @return same as input instance
	 */
	public static final <E> AtomicReferenceArray<E> clearArray (final AtomicReferenceArray<E> a)
	{
		return (null == a) ? a : clearArray(a, 0, a.length());
	}
	/**
	 * Calculates the sum of all elements in a given range - if null/empty
	 * array or bad range specified than a value of zero is returned
	 * @param a array whose elements range are to be totaled
	 * @param startPos start element index (inclusive)
	 * @param len number of elements to be calculated
	 * @return the total sum of all values in the array
	 */
	public static final long sumArray (final AtomicIntegerArray a, final int startPos, final int len)
	{
		if ((null == a) || (startPos < 0) || (len <= 0))
			return 0L;

		long		aTotal=0L;
		final int	maxPos=Math.min(startPos + len, a.length());
		for (int	aIndex=startPos; aIndex < maxPos; aIndex++)
			aTotal += a.get(aIndex);

		return aTotal;
	}
	/**
	 * @param a array whose values are to be totaled - if null/empty then
	 * a zero value is returned
	 * @return the total sum of all values in the array
	 */
	public static final long sumArray (final AtomicIntegerArray a)
	{
		return (null == a) ? 0L : sumArray(a, 0, a.length());
	}
	/**
	 * Calculates the sum of all elements in a given range - if null/empty
	 * array or bad range specified than a value of zero is returned
	 * @param a array whose elements range are to be totaled
	 * @param startPos start element index (inclusive)
	 * @param len number of elements to be calculated
	 * @return the total sum of all values in the array
	 */
	public static final long sumArray (final AtomicLongArray a, final int startPos, final int len)
	{
		if ((null == a) || (startPos < 0) || (len <= 0))
			return 0L;

		long		aTotal=0L;
		final int	maxPos=Math.min(startPos + len, a.length());
		for (int	aIndex=startPos; aIndex < maxPos; aIndex++)
			aTotal += a.get(aIndex);

		return aTotal;
	}
	/**
	 * @param a array whose values are to be totaled - if null/empty then
	 * a zero value is returned
	 * @return the total sum of all values in the array
	 */
	public static final long sumArray (final AtomicLongArray a)
	{
		return (null == a) ? 0L : sumArray(a, 0, a.length());
	}
}
