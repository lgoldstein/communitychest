package net.community.chest.util.map;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.resources.PropertyAccessor;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Useful extra logic added to {@link java.util.AbstractMap} implementation to make
 * it more robust/flexible at the same time
 * 
 * @param <K> Generic key type
 * @param <V> Generic value type
 * @author Lyor G.
 * @since Jun 13, 2007 12:09:02 PM
 */
public abstract class ExtendedAbstractMap<K,V> extends BaseTypedValuesContainer<V>
		implements Map<K,V>, PropertyAccessor<K,V> {
	/**
	 * Allocates an array of specified size
	 * @param size size to allocate - may not be <= 0
	 * @return allocated array
	 * @throws IllegalArgumentException if non-positive size requested
	 */
	@SuppressWarnings("unchecked")
	protected V[] allocateValuesArray (final int size) throws IllegalArgumentException
	{
		if (size <= 0)
			throw new IllegalArgumentException("allocateValuesArray(" + size + ") bad/illegal size");

		return (V[]) Array.newInstance(getValuesClass(), size);
	}

	private final Class<K>	_keysClass;
	public final /* no cheating */ Class<K> getKeysClass ()
	{
		return _keysClass;
	}

	protected ExtendedAbstractMap (Class<K> kClass, Class<V> objClass)
	{
		super(objClass);

		if (null == (_keysClass=kClass))
			throw new IllegalArgumentException("No key(s) class instance provided");
	}
	/* Declare negative size map as empty as well
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty ()
	{
		return (size() <= 0);
	}
	/* Ignores null/empty map
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
	public void putAll (final Map<? extends K, ? extends V> t)
	{
		final Collection<? extends Entry<? extends K, ? extends V>>	eSet=((null == t) || (t.size() <= 0)) ? null : t.entrySet();
		if ((eSet != null) && (eSet.size() > 0))
		{
			for (final Entry<? extends K, ? extends V> e : eSet)
			{
				if (e != null)	// should not be otherwise
					put(e.getKey(), e.getValue());
			}
		}
	}
	/* Checks if {@link #get(Object)} returns non-null
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey (Object key)
	{
		return (get(key) != null);
	}
	/*
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue (final Object value)
	{
		final Collection<? extends V>	vals=values();
		if ((null == vals) || (vals.size() <= 0))
			return false;

		for (final V v : vals)
		{
			if (null == value)
			{
				if (null == v)
					return true;
			}
			else if (value.equals(v))
				return true;
		}

		// this point is reached if no match found
		return false;
	}
	/*
	 * @see net.community.chest.resources.PropertyAccessor#getProperty(java.lang.Object)
	 */
	@Override
	public V getProperty (K key)
	{
		return get(key);
	}
}
