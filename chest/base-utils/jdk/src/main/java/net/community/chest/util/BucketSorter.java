package net.community.chest.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 18, 2007 1:40:27 PM
 */
public final class BucketSorter {
	private BucketSorter ()
	{
		// no instance
	}
	/**
	 * Sorts into "buckets" the supplied values array
	 * @param <V> Type of sorted object
	 * @param ar values array to be sorted - if null/empty nothing is done.
	 * <B>Note:</B> any <I>null</I> elements are ignored
	 * @param c {@link BucketComparator} to be used to determine each
	 * element's index
	 * @param maxBuckets max. expected number of buckets (also max. expected
	 * element index + 1)
	 * @return array of {@link Collection}-s each containing the elements that
	 * had the same index from the call to {@link BucketComparator#getBucketIndex(Object)}.
	 * Null elements signal the fact that none of the elements had the specified index.
	 * If no elements were sorted then the entire return value is null
	 * @throws IllegalArgumentException if null comparator or non-positive
	 * max. buckets value
	 * @throws IndexOutOfBoundsException if element bucket index exceeds max.
	 * specified buckets or negative
	 */
	public static final <V> Collection<V>[] sort (final V[] ar, final BucketComparator<V> c, final int maxBuckets)
		throws IllegalArgumentException, IndexOutOfBoundsException
	{
		if ((null == ar) || (ar.length <= 0))
			return null;
		if (null == c)
			throw new IllegalArgumentException("No bucket sort comparator");
		if (maxBuckets <= 0)
			throw new IllegalArgumentException("Bad/Illegal max. buckets value: " + maxBuckets);

		Collection<V>[]	res=null;
		for (final V value : ar)
		{
			if (null == value)	// should not happen
				continue;

			final int	vIndex=c.getBucketIndex(value);
			if ((vIndex < 0) || (vIndex >= maxBuckets))
				throw new IndexOutOfBoundsException("Bad/Illegal bucket index (" + vIndex + ") for value=" + value);
			if (null == res)
			{
				@SuppressWarnings("unchecked")
				final Collection<V>[]	ca=new Collection[maxBuckets];
				res = ca;
			}

			Collection<V>	cv=res[vIndex];
			if (null == cv)
			{
				cv = new LinkedList<V>();
				res[vIndex] = cv;
			}

			cv.add(value);
		}

		return res;
	}
	/**
	 * @param <K> Generic key type
	 * @param <V> Generic value type
	 * @param ar values array to be sorted - if null/empty nothing is done.
	 * <B>Note:</B> any <I>null</I> elements are ignored
	 * @param c {@link BucketMapper} instance to use
	 * @param m result {@link Map} - key=same as {@link BucketMapper#getBucketKey(Object)}
	 * return type, value={@link Collection} of values mapped to the same key.
	 * <B>Note:</B> the map object should be constructed but may contain no
	 * mapped {@link Collection}-s yet - one will be automatically allocated
	 * if an element is mapped to a key for which no collection is available yet.
	 * If it already contains such mapped collection, then new value is
	 * <U>add</U>-ed to the existing collection
	 * @return same as input map
	 * @throws IllegalArgumentException if null mapper
	 * @throws IndexOutOfBoundsException if null key returned from call
	 * to {@link BucketMapper#getBucketKey(Object)}
	 */
	public static final <K,V> Map<K,Collection<V>> sort (final V[] ar, final BucketMapper<K, V> c, final Map<K,Collection<V>> m)
		throws IllegalArgumentException, IndexOutOfBoundsException
	{
		if ((null == ar) || (ar.length <= 0))
			return null;
		if (null == c)
			throw new IllegalArgumentException("No bucket sort comparator");
		if (null == m)
			throw new IllegalArgumentException("No bucket map result");

		for (final V value : ar)
		{
			if (null == value)	// should not happen
				continue;

			final K key=c.getBucketKey(value);
			if (null == key)	// not allowed
				throw new IndexOutOfBoundsException("Null key for bucket value=" + value);

			Collection<V>	vc=m.get(key);
			if (null == vc)
			{
				vc = new LinkedList<V>();
				m.put(key, vc);
			}

			vc.add(value);
		}

		return m;
	}

	public static final <K,V> Map<K,Collection<V>> sort (final V[] ar, final BucketMapper<K, V> c)
	{
		if ((null == ar) || (ar.length <= 0))
			return null;
		else
			return sort(ar, c, new HashMap<K, Collection<V>>(ar.length, 1.0f));
	}
}
