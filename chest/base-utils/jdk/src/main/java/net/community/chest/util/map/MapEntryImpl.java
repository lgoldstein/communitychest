package net.community.chest.util.map;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.community.chest.CoVariantReturn;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * Copyright 2007 as per GPLv2
 * 
 * @param <K> {@link java.util.Map.Entry} key type 
 * @param <V> {@link java.util.Map.Entry} value type
 * @author Lyor G.
 * @since Jun 13, 2007 1:19:29 PM
 */
public class MapEntryImpl<K,V> implements Map.Entry<K,V>, Cloneable {
	private K	_key	/* =null */;
	/*
	 * @see java.util.Map.Entry#getKey()
	 */
	@Override
	public K getKey ()
	{
		return _key;
	}
	/**
	 * @param key new key to set
	 * @return previous key
	 */
	public K setKey (final K key)
	{
		final K prev=getKey();
		_key = key;
		return prev;
	}

	private V	_value	/* =null */;
	/*
	 * @see java.util.Map.Entry#getValue()
	 */
	@Override
	public V getValue ()
	{
		return _value;
	}
	/*
	 * @see java.util.Map.Entry#setValue(java.lang.Object)
	 */
	@Override
	public V setValue (final V value)
	{
		final V	prev=getValue();
		_value = value;
		return prev;
	}
	/**
	 * Set key+value in the same call
	 * @param key The key to be set
	 * @param value The value to be set
	 */
	public void setContent (K key, V value)
	{
		setKey(key);
		setValue(value);
	}

	public MapEntryImpl (final K key, final V value)
	{
		_key = key;
		_value = value;
	}

	public MapEntryImpl (K key)
	{
		this(key, null);
	}

	public MapEntryImpl ()
	{
		this(null, null);
	}

	public MapEntryImpl (Map.Entry<? extends K,? extends V> e)
	{
		this ((null == e) ? null : e.getKey(), (null == e) ? null : e.getValue());
	}
	/**
	 * @return TRUE if <U>both</U> the key and value are <code>null</code>
	 */
	public boolean isEmpty ()
	{
		return (null == getKey()) && (null == getValue());
	}
	/**
	 * Null-ifies the key and value 
	 */
	public void clear ()
	{
		setKey(null);
		setValue(null);
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@CoVariantReturn
	public Map.Entry<K,V> clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if ((null == obj) || (!(obj instanceof Map.Entry<?,?>)))
			return false;
		if (this == obj)
			return true;

		final Map.Entry<?,?>	me=(Map.Entry<?,?>) obj;
		return AbstractComparator.compareObjects(getKey(), me.getKey())
			&& AbstractComparator.compareObjects(getValue(), me.getValue())
			;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return ClassUtil.getObjectHashCode(getKey())
			 + ClassUtil.getObjectHashCode(getValue())
			 ;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return getKey() + "=" + getValue();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class ByKeyComparator extends AbstractComparator<Map.Entry> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7281552086881832444L;
		private Comparator<?>	_kc;
		public Comparator<?> getComparator ()
		{
			return _kc;
		}

		public void setComparator (Comparator<?> kc)
		{
			_kc = kc;
		}

		public ByKeyComparator (Comparator<?> kc, boolean ascending)
		{
			super(Map.Entry.class, !ascending);
			_kc = kc;
		}

		/*
		 * @see com.emc.common.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compareValues (Map.Entry e1, Map.Entry e2)
		{
			final Object		k1=(null == e1) ? null : e1.getKey(),
								k2=(null == e2) ? null : e2.getKey();
			final Comparator	kc=getComparator();
			if (null == kc)
				throw new IllegalStateException("compareValues(keys) " + k1 + "/" + k2 + " no comparator"); 
			return kc.compare(k1, k2);
		}
	}

	@SafeVarargs
	public static final <K,V> Map.Entry<K,V>[] sortByKey (Comparator<? super K> kc, boolean ascending, Map.Entry<K,V> ... entries)
	{
		if ((entries != null) && (entries.length > 1))
			Arrays.sort(entries, new ByKeyComparator(kc, ascending));
		return entries;
	}

	public static final <K,V> List<Map.Entry<K,V>> sortByKey (Comparator<? super K> kc, boolean ascending, List<Map.Entry<K,V>> entries)
	{
		if ((entries != null) && (entries.size() > 1))
			Collections.sort(entries, new ByKeyComparator(kc, ascending));
		return entries;
	}
}
