package net.community.chest.util.map;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.math.LongsComparator;
import net.community.chest.util.map.entries.LongsMapEntry;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @param <V> Type of mapped value
 * @author Lyor G.
 * @since Oct 23, 2007 12:05:41 PM
 */
public class LongsMap<V> extends NumbersMap<Long,V> implements PubliclyCloneable<LongsMap<V>> {
	private static final long serialVersionUID = 6743141074432098265L;
	/**
	 * @param location location indication
	 * @param fromKey start key in range (inclusive)
	 * @param toKey end key in range (exclusive)
	 * @return {@link #getExceptionLocation(String)} + "[" + from + "-" + to + "]
	 */
	protected String getRangeExceptionLocation (final String location, final long fromKey, final long toKey)
	{
		return getExceptionLocation(location) + "[" + fromKey + "-" + toKey + "]";
	}
	/**
	 * Current key values array - empty places are marked with {@link Integer#MAX_VALUE}
	 */
	private long[]	_keyVals	/* =null */;
	protected long[] getKeys ()
	{
		return _keyVals;
	}
	/*
	 * @see net.community.chest.util.map.NumbersMap#markEmptySpots(int)
	 */
	@Override
	protected void markEmptySpots (final int fromIndex)
		throws IllegalStateException, IllegalArgumentException
	{
		final long[] k=getKeys();
		final int	numKeys=(null == k) ? 0 : k.length;
		final V[]	o=getObjects();
		final int	numObjects=(null == o) ? 0 : o.length;

		if (numKeys != numObjects)
			throw new IllegalStateException(getExceptionLocation("markEmptySpots") + "(" + fromIndex + ") keys(" + numKeys + ")/objects(" + numObjects + ") arrays lengths mismatch");
		if ((fromIndex < 0) || (fromIndex > numKeys) || (fromIndex > numObjects))
			throw new IllegalArgumentException(getExceptionLocation("markEmptySpots") + "(" + fromIndex + ") bad/illegal index (range=0-" + numKeys + ")"); 

		for (int	i=fromIndex; i < numKeys; i++)
		{
			k[i] = Long.MAX_VALUE;
			o[i] = null;
		}
	}

	public Collection<V> values (final long fromKey, final long toKey)
	{
		final Collection<? extends Entry<Long,V>>	eSet=entrySet(fromKey, toKey);
		if ((null == eSet) || (eSet.size() <= 0))
			return null;

		Collection<V>	ret=null;
		for (final Entry<Long,V> e : eSet)
		{
			final V	value=(null == e) /* should not happen */ ? null : e.getValue();
			if (null == value)	// should not happen
				continue;

			if (null == ret)
				ret = new LinkedList<V>();
			ret.add(value);
		}

		return ret;
	}
	/*
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection<V> values ()
	{
		return values(Long.MIN_VALUE, Long.MAX_VALUE);
	}
	/**
	 * Constructor
	 * @param objClass class of object to be used (required for creating
	 * arrays) - may NOT be null
	 * @param initialSize allocated room initially - may be zero provided grow
	 * size is non-zero. May NOT be negative
	 * @param growSize how much to make room automatically if needed - may be
	 * zero provided initial size is non-zero (in which case, any attempt to
	 * grow beyond the limits will cause an exception - unless subsequent
	 * call(s) to {@link #setGrowSize(int)} or {@link #grow(int)}
	 * @throws IllegalArgumentException if no class, negative values or both zero
	 */
	public LongsMap (final Class<V> objClass, final int initialSize, final int growSize) throws IllegalArgumentException
		/* IllegalStateException due to {@link #markEmptySpots()} - should never happen */
	{
		super(Long.class, objClass, initialSize, growSize);
		
		if (initialSize > 0)
			_keyVals = new long[initialSize];
		markEmptySpots();
	}
	/*
	 * @see net.community.chest.util.map.NumbersMap#grow(int)
	 */
	@Override
	public void grow (final int growSize) throws IllegalArgumentException, IllegalStateException
	{
		if (growSize < 0)
			throw new IllegalArgumentException(getGrowExceptionLocation(growSize) + " negative requested size");

		if (growSize > 0)
		{
			final long[] 	k=getKeys();
			final int		numKeys=(null == k) ? 0 : k.length;
			final V[]		o=getObjects();
			final int		numObjects=(null == o) ? 0 : o.length;

			if (numKeys != numObjects)
				throw new IllegalStateException(getGrowExceptionLocation(growSize) + " keys(" + numKeys + ")/objects(" + numObjects + ") arrays lengths mismatch");

			final int	newSize=numKeys + growSize, curSize=size();
			_keyVals = new long[newSize];
			if (curSize > 0)
				System.arraycopy(k, 0, _keyVals, 0, curSize);

			growObjects(newSize);
			markEmptySpots();
		}
	}
	/**
	 * Useful string for {@link #findEntryIndex(long)} exceptions text
	 * @param key key value requested for mapping
	 * @return {@link #getExceptionLocation(String)} + "(" + key + ")"
	 */
	protected String getFindEntryIndexExceptionLocation (final long key)
	{
		return getExceptionLocation("findEntryIndex") + "(" + key + ")";
	}
	/**
	 * Attempts to find the location for the key
	 * @param key key whose location is requested
	 * @return >=0 if successful, -(insertionPoint)-1 otherwise
	 * @throws IllegalArgumentException if key is {@link Long#MAX_VALUE}
	 * @throws IllegalStateException if {@link #size()} greater than keys
	 * array length (which should not happen) or found match beyond the
	 * {@link #size()} - which means that {@link Long#MAX_VALUE} was
	 * found
	 * @see Arrays#binarySearch(int[], int)
	 */
	protected int findEntryIndex (final long key) throws IllegalArgumentException, IllegalStateException
	{
		if (Long.MAX_VALUE == key)
			throw new IllegalArgumentException(getFindEntryIndexExceptionLocation(key) + " illegal key value");

		final int	curSize=size();
		if (curSize <= 0)	// if no current entries, then obviously first position
			return (-1);

		final long[]	k=getKeys();
		final int		numKeys=(null == k) ? 0 : k.length;
		if (curSize > numKeys)	// impossible - should not happen 
			throw new IllegalStateException(getFindEntryIndexExceptionLocation(key) + " mismatched size(" + curSize + ")/keys(" + numKeys + ") values");

		final int	eIndex=Arrays.binarySearch(k, key);
		/* 
		 * If found, it cannot be beyond the current size since it is assumed
		 * that ALL entries beyond current size are marked with
		 * Long.MAX_VALUE
		 */
		if (eIndex >= curSize)
			throw new IllegalStateException(getFindEntryIndexExceptionLocation(key) + " found entry at index=" + eIndex + " beyond current size (" + curSize + ")");

		return eIndex;
	}
	/**
	 * Copyright 2007 as per GPLv2
	 * 
	 * Compares 2 entries based on their key
	 * 
	 * @author Lyor G.
	 * @since Jun 10, 2007 5:11:51 PM
	 */
	public static final class LongEntriesComparator extends NumberEntriesComparator {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6516762461710760149L;

		public LongEntriesComparator ()
		{
			super();
		}
		/*
		 * @see net.community.chest.util.map.NumbersMap.NumberEntriesComparator#compareNumbers(java.lang.Number, java.lang.Number)
		 */
		@Override
		protected int compareNumbers (Number n1, Number n2)
		{
			return LongsComparator.compare((null == n1) ? 0L : n1.longValue(), (null == n2) ? 0L : n2.longValue());
		}
		
		public static final LongEntriesComparator	DEFAULT=
			new LongEntriesComparator();
	}
	/*
	 * @see net.community.chest.util.map.NumbersMap#getEntryComparator()
	 */
	@Override
	public Comparator<Entry<? extends Number,?>> getEntryComparator ()
	{
		return LongEntriesComparator.DEFAULT;
	}

	public Set<Map.Entry<Long, V>> entrySet (final long fromKey, final long toKey)
	{
		if (fromKey > toKey)
			throw new IllegalArgumentException(getRangeExceptionLocation("entrySet", fromKey, toKey) + " inverted range");

		final int	numItems=size();
		if (numItems <= 0)
			return null;

		final long[]	ks=getKeys();
		if ((null == ks) || (ks.length < numItems))	// should not happen
			throw new IllegalStateException(getRangeExceptionLocation("entrySet", fromKey, toKey) + " mismatched size (" + numItems + ") vs. key set size (" + ((null == ks) ? 0 : ks.length) + ")");

		final V[]	vs=getObjects();
		if ((null == vs) || (vs.length < numItems))	// should not happen
			throw new IllegalStateException(getRangeExceptionLocation("entrySet", fromKey, toKey) + " mismatched size (" + numItems + ") vs. values set size (" + ((null == vs) ? 0 : vs.length) + ")");

		TreeSet<Entry<Long, V>>	ts=null;
		for (int	eIndex=0; eIndex < numItems; eIndex++)
		{
			final long	k=ks[eIndex];
			if (k >= toKey)	// no need to continue if exceed upper key
				break;

			if (k >= fromKey)
			{
				final LongsMapEntry<V>	e=new LongsMapEntry<V>(Long.valueOf(k), vs[eIndex]);
				if (null == ts)
					ts = new TreeSet<Entry<Long,V>>(getEntryComparator());
				ts.add(e);
			}
		}

		return ts;
	}
	/*
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set<Entry<Long, V>> entrySet ()
	{
		return entrySet(Long.MIN_VALUE, Long.MAX_VALUE);
	}
	/**
	 * Useful string for {@link #put(long, Object)} exceptions
	 * @param key key value requested for mapping
	 * @param obj object value requested for mapping
	 * @return {@link #getExceptionLocation(String)} + "(" + key + "=>" + String.valueOf(obj) + ")"
	 */
	protected String getPutExceptionLocation (final long key, final V obj)
	{
		return getExceptionLocation("put") + "(" + key + "=>" + String.valueOf(obj) + ")";
	}
	/**
	 * Maps an <I>int</I> key to a (non-null) {@link Object}
	 * @param key key value - may NOT be {@link Long#MAX_VALUE}
	 * @param obj object value - may NOT be <I>null</I>
	 * @return previous object - <I>null</I> if object mapped for the first time
	 * @throws NullPointerException if null object
	 * @throws IllegalArgumentException bad/illegal key
	 * @throws IllegalStateException cannot grow, null previous mapping, etc.
	 */
	public V put (final long key, final V obj)
		throws NullPointerException, IllegalArgumentException, IllegalStateException
	{
		if (Long.MAX_VALUE == key)
			throw new IllegalArgumentException(getPutExceptionLocation(key, obj) + " bad/illegal key");
		if (null == obj)
			throw new NullPointerException(getPutExceptionLocation(key, obj) + " bad/illegal object");

		final int	eIndex=findEntryIndex(key);
		V[]			o=getObjects();
		int			numObjects=(null == o) ? 0 : o.length;
		if (eIndex >= 0)
		{
			// should not happen since keys/objects array(s) are supposed to be of same length
			if (eIndex >= numObjects)
				throw new IllegalStateException(getPutExceptionLocation(key, obj) + " previous index (" + eIndex + ") beyond objects arrays length (" + numObjects + ")");

			final V	prev=o[eIndex];
			// should not happen since we do not put such objects in the first place
			if (null == prev)
				throw new IllegalStateException(getPutExceptionLocation(key, obj) + " empty previous object at index=" + eIndex);

			o[eIndex] = obj;
			return prev;
		}
		else	// new location recommended
		{
			// @see Arrays#binarySearch(int[], int)
			final int	nIndex=(-1) - eIndex, curSize=size();
			// if recommended location/new size beyond current capacity then (try to) grow
			if ((nIndex >= numObjects) || (curSize >= numObjects)) 
			{
				final int	gSize=getGrowSize();
				if (gSize <= 0)
					throw new IllegalStateException(getPutExceptionLocation(key, obj) + " cannot grow by " + gSize + " for index=" + nIndex);
				grow(gSize);	// will throw an exception if unable

				o = getObjects();
				numObjects = (null == o) ? 0 : o.length;
			}

			final long[]	k=getKeys();
			final int		numKeys=(null == k) ? 0 : k.length;

			// if recommended index falls within currently occupied entries then need to make room for it
			if (nIndex < curSize)
			{
				if (numKeys != numObjects)	// should not happen
					throw new IllegalStateException(getPutExceptionLocation(key, obj) + " keys(" + numKeys + ")/objects(" + numObjects + ") arrays lengths mismatch");

				for (int	i=numKeys-1; i > nIndex; i--)
				{
					k[i] = k[i - 1];
					o[i] = o[i - 1];
				}

				// mark entry as empty (at least for a little while)
				k[nIndex] = Long.MAX_VALUE;
				o[nIndex] = null;
			}
			//	if recommended index falls beyond current size then it MUST be the very next one
			else if (nIndex != curSize) 
			{
				throw new IllegalStateException(getPutExceptionLocation(key, obj) + " recommended location (" + nIndex + ") beyond end of array(s)=" + curSize);
			}

			k[nIndex] = key;
			o[nIndex] = obj;
			updateSize(1);

			return null;	// signal that this was a new object
		}
	}
	/*
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V put (final Long key, final V value) throws IllegalArgumentException, IllegalStateException
	{
		if (null == key)
			throw new NullPointerException(getExceptionLocation("put") + "(" + value + ") null key not allowed");
		return put(key.longValue(), value);
	}
	/**
	 * Useful string for {@link #remove(long)} exceptions text
	 * @param key key value requested for removal
	 * @return {@link #getExceptionLocation(String)} + "(" + key + ")"
	 */
	protected String getRemoveExceptionLocation (final long key)
	{
		return getExceptionLocation("remove") + "(" + key + ")";
	}
	/**
	 * Removes specified key (if exists)
	 * @param key key to be removed - cannot be {@link Long#MAX_VALUE}
	 * @return removed object - <I>null</I> if not in the map to begin with
	 * @throws IllegalArgumentException bad/illegal key
	 * @throws IllegalStateException null previous mapping, etc.
	 */
	public V remove (final long key) throws IllegalArgumentException, IllegalStateException
	{
		final int	eIndex=findEntryIndex(key);
		if (eIndex < 0)	// OK if not found
			return null;

		final long[] 	k=getKeys();
		final int		numKeys=(null == k) ? 0 : k.length;
		final V[]		o=getObjects();
		final int		numObjects=(null == o) ? 0 : o.length;

		if (numKeys != numObjects)
			throw new IllegalStateException(getRemoveExceptionLocation(key) + " keys(" + numKeys + ")/objects(" + numObjects + ") arrays lengths mismatch");

		final V	prev=o[eIndex];
		// not allowed to put null(s)/MAX_VALUE(s) in the first place
		if ((null == prev) || (Long.MAX_VALUE == k[eIndex]))
			throw new IllegalStateException(getRemoveExceptionLocation(key) + " empty previous object at index=" + eIndex);

		for (int	i=eIndex; i < (numKeys-1); i++)
		{
			k[i] = k[i+1];
			o[i] = o[i+1];
		}

		// just making sure
		k[numKeys - 1] = Long.MAX_VALUE;
		o[numObjects - 1] = null;
		updateSize(-1);

		return prev;
	}
	/*
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public V remove (Object key)
	{
		if (key instanceof Number)
			return remove(((Number) key).longValue());
		return null;
	}

	public Collection<V> remove (final long fromKey, final long toKey)
	{
		if (fromKey > toKey)
			throw new IllegalArgumentException(getRangeExceptionLocation("countKeysInRange", fromKey, toKey) + " inverted range");

		final int	numItems=size();
		if (numItems <= 0)
			return null;

		final long[]	ks=getKeys();
		if ((null == ks) || (ks.length < 1))
			throw new IllegalStateException(getRangeExceptionLocation("countKeysInRange", fromKey, toKey) + " no keys values");

		Collection<V>	vals=null;
		for (final long k : ks)
		{
			// no need to contine if exceeded top key
			if (k >= toKey)
				break;

			if (k >= fromKey)
			{
				final V	v=remove(k);
				if (v != null)
				{
					if (null == vals)
						vals = new LinkedList<V>();
					vals.add(v);
				}
			}
		}

		return vals;
	}
	/**
	 * Useful string for {@link #get(long)} exceptions text
	 * @param key key value requested for removal
	 * @return {@link #getExceptionLocation(String)} + "(" + key + ")"
	 */
	protected String getGetExceptionLocation (final long key)
	{
		return getExceptionLocation("remove") + "(" + key + ")";
	}
	/**
	 * Checks if specified key exists in map
	 * @param key key to be checked - may NOT be {@link Long#MAX_VALUE}
	 * @return found object (null if not found)
	 * @throws IllegalArgumentException if illegal key
	 * @throws IllegalStateException internal array(s) length(s) mismatches
	 */
	public V get (final long key) throws IllegalArgumentException, IllegalStateException
	{
		final int	eIndex=findEntryIndex(key);
		if (eIndex < 0)	// OK if not found
			return null;
		
		final V[]	o=getObjects();
		final int	numObjects=(null == o) ? 0 : o.length, curSize=size();
		// returned index MUST be within current size and number of objects
		if ((eIndex >= numObjects) || (eIndex >= curSize))
			throw new IllegalStateException(getGetExceptionLocation(key) + " entry index (" + eIndex + ") beyond objects array length (" + numObjects + ") or current size (" + curSize + ")");

		final V	res=o[eIndex];
		// we do not allow to put such objects in the first place
		if (null == res)
			throw new IllegalStateException(getGetExceptionLocation(key) + " null object at index=" + eIndex);

		return res;
	}
	/*
	 * @see java.util.Map#get(java.lang.Object)
	 * @see #get(int)
	 */
	@Override
	public V get (final Object key) throws IllegalArgumentException, IllegalStateException
	{
		if (key instanceof Number)
			return get(((Number) key).longValue());
		return null;
	}

	public Set<Long> keySet (final long fromKey, final long toKey)
	{
		final Collection<? extends Entry<Long,V>>	eSet=entrySet(fromKey, toKey);
		if ((null == eSet) || (eSet.size() <= 0))
			return null;

		Set<Long>	ret=null;
		for (final Entry<Long,V> e : eSet)
		{
			final Long	k=(null == e) /* should not happen */ ? null : e.getKey();
			if (null == k)	// should not happen
				continue;

			if (null == ret)
				ret = new TreeSet<Long>(comparator());
			ret.add(k);
		}

		return ret;
	}
	/*
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set<Long> keySet ()
	{
		return keySet(Long.MIN_VALUE, Long.MAX_VALUE);
	}
	/*
	 * @see java.util.SortedMap#comparator()
	 */
	@Override
	public Comparator<? super Long> comparator ()
	{
		return LongsComparator.ASCENDING;
	}

	public Long firstKey (final long fromKey, final long toKey) throws NoSuchElementException
	{
		final int	numItems=size();
		if (numItems <= 0)
			throw new NoSuchElementException(getRangeExceptionLocation("firstKey", fromKey, toKey) + " empty map");

		final long[]	ks=getKeys();
		if ((null == ks) || (ks.length < 1))
			throw new IllegalStateException(getRangeExceptionLocation("firstKey", fromKey, toKey) + " no keys values");

		for (final long k : ks)
		{
			// no need to check further if exceed upper limit key
			if (k >= toKey)
				break;

			if (k >= fromKey)
			{
				if (Long.MAX_VALUE == k)	// should not happen
					throw new IllegalStateException(getRangeExceptionLocation("firstKey", fromKey, toKey) + " marked empty spot key value");

				return Long.valueOf(k);
			}
		}

		throw new NoSuchElementException(getRangeExceptionLocation("firstKey", fromKey, toKey) + " no match found");
	}
	/*
	 * @see java.util.SortedMap#firstKey()
	 * @throws NoSuchElementException if no element found
	 */
	@Override
	public Long firstKey () throws NoSuchElementException
	{
		return firstKey(Long.MIN_VALUE, Long.MAX_VALUE);
	}

	public Long lastKey (final long fromKey, final long toKey) throws NoSuchElementException
	{
		final int	numItems=size();
		if (numItems <= 0)
			throw new NoSuchElementException(getRangeExceptionLocation("lastKey", fromKey, toKey) + " empty map");

		final long[]	ks=getKeys();
		final int		numKeys=(null == ks) ? 0 : ks.length;
		if (numKeys < numItems)
			throw new IllegalStateException(getRangeExceptionLocation("lastKey", fromKey, toKey) + " no keys values");

		for (int	kIndex=numKeys-1; kIndex >= 0; kIndex--)
		{
			final long	k=ks[kIndex];
			// no need to check further if below lower limit key
			if (k < fromKey)
				break;

			if (k < toKey)
			{
				if (Long.MAX_VALUE == k)	// should not happen
					throw new IllegalStateException(getRangeExceptionLocation("lastKey", fromKey, toKey) + " marked empty spot key value");

				return Long.valueOf(k);
			}
		}

		throw new NoSuchElementException(getRangeExceptionLocation("lastKey", fromKey, toKey) + " no match found");
	}
	/*
	 * @see java.util.SortedMap#lastKey()
	 * @throws NoSuchElementException if no element found
	 */
	@Override
	public Long lastKey () throws NoSuchElementException
	{
		return lastKey(Long.MIN_VALUE, Long.MAX_VALUE);
	}

	public int countKeysInRange (final long fromKey, final long toKey)
	{
		if (fromKey > toKey)
			throw new IllegalArgumentException(getRangeExceptionLocation("countKeysInRange", fromKey, toKey) + " inverted range");

		final int	numItems=size();
		if (numItems <= 0)
			return 0;

		final long[]	k=getKeys();
		if ((null == k) || (k.length < numItems))
			throw new IllegalStateException(getRangeExceptionLocation("countKeysInRange", fromKey, toKey) + " no keys values");

		int	numKeys=0;
		for (final long kv : k)
		{
			// if current key exceeded max. value, no need to check further
			if (kv >= toKey)
				return numKeys;
			// count key only if over the min. threshold
			if (kv >= fromKey)
				numKeys++;
		}

		return numKeys;
	}
	/*
	 * @see net.community.chest.util.AbstractSortedMap#countKeysInRange(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int countKeysInRange (final Long fromKey, final Long toKey)
	{
		if ((null == fromKey) || (null == toKey))
			throw new NullPointerException(getExceptionLocation("countKeysInRange") + " null from(" + fromKey + ")/to(" + toKey + ") key(s)");

		return countKeysInRange(fromKey.longValue(), toKey.longValue());
	}
	/* NOTE: changes in the sub-map are NOT reflected in the original map
	 * @return null if no keys in range
	 * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
	 */
	@Override
	public SortedMap<Long, V> subMap (final Long fromKey, final Long toKey)
	{
		if ((null == fromKey) || (null == toKey))
			throw new NullPointerException(getExceptionLocation("subMap") + " null from(" + fromKey + ")/to(" + toKey + ") key(s)");

		final long	fkValue=fromKey.longValue(), tkValue=toKey.longValue();
		if (fkValue > tkValue)
			throw new IllegalArgumentException(getExceptionLocation("subMap") + " inverted range: [" + fromKey + " - " + toKey + "]");

		final int	numItems=size();
		if (numItems <= 0)
			return null;

		final long[]	ks=getKeys();
		if ((null == ks) || (ks.length < numItems))
			throw new IllegalStateException(getExceptionLocation("subMap") + "[" + fromKey + " - " + toKey + "] mismatched keys array size");

		final V[]	vs=getObjects();
		if ((null == vs) || (vs.length < numItems))
			throw new IllegalStateException(getExceptionLocation("subMap") + "[" + fromKey + " - " + toKey + "] mismatched objects array size");

		LongsMap<V>	res=null;
		for (int	eIndex=0; eIndex < numItems; eIndex++)
		{
			final long	key=ks[eIndex];
			if (key < fkValue)
				continue;
			if (key > tkValue)
				break;

			if (null == res)
				res = new LongsMap<V>(getValuesClass(), numItems, getGrowSize());
			res.put(key, vs[eIndex]);
		}

		return res;
	}
	// min./max. key values (inclusive)
	public static final Long	MIN_KEY=Long.valueOf(Long.MIN_VALUE),
								MAX_KEY=Long.valueOf(Long.MAX_VALUE - 1),
								TAIL_KEY=Long.valueOf(MAX_KEY.longValue() + 1);
	/*
	 * @see java.util.SortedMap#headMap(java.lang.Object)
	 */
	@Override
	public SortedMap<Long, V> headMap (final Long toKey)
	{
		return subMap(MIN_KEY, toKey);
	}
	/*
	 * @see java.util.SortedMap#tailMap(java.lang.Object)
	 */
	@Override
	public SortedMap<Long, V> tailMap (final Long fromKey)
	{
		return subMap(fromKey, TAIL_KEY);
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public LongsMap<V> clone () throws CloneNotSupportedException
	{
		@SuppressWarnings("unchecked")
		final LongsMap<V>	cm=(LongsMap<V>) super.clone();
		final long[]		ck=cm.getKeys();
		if (ck != null)
		{
			cm._keyVals = new long[ck.length];
			System.arraycopy(ck, 0, cm._keyVals, 0, ck.length);
		}

		final V[]	cv=cm.getObjects();
		if (cv != null)	// copy the objects as well
		{
			final V[]	newVals=allocateValuesArray(cv.length);
			System.arraycopy(cv, 0, newVals, 0, cv.length);
			cm.setObjects(newVals);
		}

		return cm;
	}
}
