package net.community.chest.util.map;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

import net.community.chest.util.compare.BooleansComparator;
import net.community.chest.util.map.entries.BooleansMapEntry;

/**
 * Copyright 2007 as per GPLv2
 *
 * Implements a {@link java.util.Map} whose key(s) are {@link Boolean}-s (or the
 * equivalent atomic type). The map has special handling for <I>null></I>
 * keys - they may be allowed or not - depending on the specification in the
 * constructor. In other words, the map can either contain at most 2 keys (if
 * <I>null></I>-s are not allowed) or 3 keys (if <I>null></I> are allowed)
 *
 * @param <V> Type of contained values(s)
 * @author Lyor G.
 * @since Jun 21, 2007 1:41:14 PM
 */
public class BooleansMap<V> extends AbstractSortedMap<Boolean,V> {
    private final boolean    _allowNullKey;
    public final /* no cheating */ boolean isNullKeyAllowed ()
    {
        return _allowNullKey;
    }
    /**
     * @param key The {@link Boolean} key
     * @return The zero based index of the key in the {@link #_values}
     * array - negative if <code>null</code> key and {@link #isNullKeyAllowed()}
     * returns <code>false</code>
     */
    protected int getKeyIndex (final Boolean key)
    {
        final boolean    allowNullKey=isNullKeyAllowed();
        if (null == key)
        {
            if (allowNullKey)
                return 0;
            else
                return (-1);
        }
        else
        {
            final int    offset=key.booleanValue() ? 1 : 0;
            if (allowNullKey)
                return (1 + offset);
            else
                return offset;
        }
    }

    private final V[]        _values;
    protected final V[] getObjects ()
    {
        return _values;
    }

    private final boolean[]    _keys;    // TRUE means that the value has been set
    protected final boolean[] getKeyFlags ()
    {
        return _keys;
    }

    public BooleansMap (Class<V> objClass, final boolean allowNullKey)
    {
        super(Boolean.class, objClass);

        _allowNullKey = allowNullKey;

        final int    vSize=getKeyIndex(Boolean.TRUE) + 1;
        _values = allocateValuesArray(vSize);    // all null...
        _keys = new boolean[vSize];    // all FALSE...
    }
    /*
     * @see java.util.Map#clear()
     */
    @Override
    public void clear ()
    {
        final V[] ov=getObjects();
        for (int    vIndex=0; vIndex < ov.length; vIndex++)
            ov[vIndex] = null;

        final boolean[]    kf=getKeyFlags();
        for (int    kIndex=0; kIndex < kf.length; kIndex++)
            kf[kIndex] = false;
    }
    /*
     * @see java.util.Map#size()
     */
    @Override
    public int size ()
    {
        int    sz=0;
        // simply count number of valid keys
        final boolean[]    kf=getKeyFlags();
        for (final boolean k : kf)
        {
            if (k)
                sz++;
        }

        return sz;
    }
    /*
     * @see java.util.SortedMap#comparator()
     */
    @Override
    public Comparator<? super Boolean> comparator ()
    {
        return BooleansComparator.ASCENDING;
    }
    /*
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public V get (final Object key)
    {
        final int    kIndex;
        if ((null == key) || (key instanceof Boolean))
            kIndex = getKeyIndex((Boolean) key);
        else
            kIndex = Integer.MIN_VALUE;

        final boolean[]    kf=getKeyFlags();
        if ((kIndex < 0) || (kIndex >= kf.length) || (!kf[kIndex]))
            return null;

        return getObjects()[kIndex];
    }

    public V get (final boolean key)
    {
        return get(Boolean.valueOf(key));
    }
    /*
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public V put (Boolean key, V value)
    {
        final int    kIndex=getKeyIndex(key);
        if (kIndex < 0)
            throw new IllegalArgumentException(getExceptionLocation("put") + "[" + key + "] invalid key for value=" + value);

        final boolean[]    kf=getKeyFlags();
        final V[]        ov=getObjects();
        final boolean    prevExists=kf[kIndex];
        final V            prev=prevExists ? ov[kIndex] : null;

        if (!prevExists)    // debug breakpoint
            kf[kIndex] = true;    // mark as valid key
        ov[kIndex] = value;
        return prev;
    }

    public V put (boolean key, V value)
    {
        return put(Boolean.valueOf(key), value);
    }
    /*
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public V remove (final Object key)
    {
        final int    kIndex;
        if ((null == key) || (key instanceof Boolean))
            kIndex = getKeyIndex((Boolean) key);
        else
            kIndex = Integer.MIN_VALUE;

        final boolean[]    kf=getKeyFlags();
        if ((kIndex < 0) || (kIndex >= kf.length) || (!kf[kIndex]))
            return null;

        final V[]    ov=getObjects();
        final V        prev=ov[kIndex];

        kf[kIndex] = false;    // mark as invalid
        ov[kIndex] = null;

        return prev;
    }

    public V remove (boolean key)
    {
        return remove(Boolean.valueOf(key));
    }
    /**
     * @param kIndex Key index in the {@link #_values} array
     * @return The matching {@link Boolean} key - <code>null</code>
     * if no match found.
     */
    protected Boolean fromKeyIndex (final int kIndex)
    {
        final boolean allowNullKey=isNullKeyAllowed();
        if (allowNullKey)
        {
            if (0 == kIndex)
                return null;
            else
                return Boolean.valueOf(kIndex > 1);
        }

        return Boolean.valueOf(kIndex > 0);
    }
    /*
     * @see java.util.SortedMap#firstKey()
     */
    @Override
    public Boolean firstKey ()
    {
        final boolean[]    kf=getKeyFlags();
        for (int    kIndex=0; kIndex < kf.length; kIndex++)
        {
            if (kf[kIndex])
                return fromKeyIndex(kIndex);
        }

        throw new NoSuchElementException(getExceptionLocation("firstKey") + " no match found");
    }
    /*
     * @see java.util.SortedMap#lastKey()
     */
    @Override
    public Boolean lastKey ()
    {
        final boolean[]    kf=getKeyFlags();
        for (int    kIndex=kf.length-1; kIndex >= 0; kIndex--)
        {
            if (kf[kIndex])
                return fromKeyIndex(kIndex);
        }

        throw new NoSuchElementException(getExceptionLocation("lastKey") + " no match found");
    }
    /*
     * @see java.util.Map#values()
     */
    @Override
    public Collection<V> values ()
    {
        final boolean[]    kf=getKeyFlags();
        final V[]        ov=getObjects();
        Collection<V>    vals=null;
        for (int    kIndex=0; kIndex < kf.length; kIndex++)
        {
            if (kf[kIndex])
            {
                if (null == vals)
                    vals = new LinkedList<V>();
                vals.add(ov[kIndex]);
            }
        }

        return vals;
    }
    /*
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<Boolean> keySet ()
    {
        final boolean[]    kf=getKeyFlags();
        Set<Boolean>    keys=null;
        for (int    kIndex=0; kIndex < kf.length; kIndex++)
        {
            if (kf[kIndex])
            {
                if (null == keys)
                    keys = new TreeSet<Boolean>(comparator());
                keys.add(fromKeyIndex(kIndex));
            }
        }

        return keys;
    }
    /**
     * Copyright 2007 as per GPLv2
     *
     * Compares 2 entries based on their key
     * @param <V> Type of value being compared
     * @author Lyor G.
     * @since Jun 21, 2007 14:28:51
     */
    protected static final class EntriesComparator<V> implements Comparator<Entry<Boolean,V>>, Serializable {
        /**
         *
         */
        private static final long serialVersionUID = -2827350353807091431L;
        protected EntriesComparator ()
        {
            super();
        }
        /*
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare (final Entry<Boolean, V> o1, final Entry<Boolean, V> o2)
        {
            return BooleansComparator.ASCENDING.compare((null == o1) ? null : o1.getKey(), (null == o2) ? null : o2.getKey());
        }
    }
    /*
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<Entry<Boolean, V>> entrySet ()
    {
        final boolean[]            kf=getKeyFlags();
        final V[]                ov=getObjects();
        Set<Entry<Boolean, V>>    s=null;
        for (int    kIndex=0; kIndex < kf.length; kIndex++)
        {
            if (kf[kIndex])
            {
                if (null == s)
                    s = new TreeSet<Entry<Boolean,V>>(new EntriesComparator<V>());
                s.add(new BooleansMapEntry<V>(fromKeyIndex(kIndex), ov[kIndex]));
            }
        }

        return s;
    }
    /*
     * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
     */
    @Override
    public SortedMap<Boolean, V> subMap (final Boolean fromKey, final Boolean toKey)
    {
        if (null == toKey)
        {
            if (fromKey != null)
                throw new IllegalArgumentException(getExceptionLocation("subMap") + " inverted range: null - " + toKey);
        }
        else if (fromKey != null)
        {
            if (fromKey.compareTo(toKey) > 0)
                throw new IllegalArgumentException(getExceptionLocation("subMap") + " inverted range: " + fromKey + " - " + toKey);
        }

        BooleansMap<V>     res=null;
        final boolean[]    kf=getKeyFlags();
        final V[]        ov=getObjects();
        for (int    kIndex=0; kIndex < kf.length; kIndex++)
        {
            if (kf[kIndex])
            {
                final Boolean    key=fromKeyIndex(kIndex);
                if (fromKey != null)
                {
                    if ((null == key) || (fromKey.compareTo(key) > 0))
                        continue;
                }

                if (toKey != null)
                {
                    if ((key != null) && (toKey.compareTo(key) < 0))
                        break;
                }

                if (null == res)
                    res = new BooleansMap<V>(getValuesClass(), isNullKeyAllowed());
                res.put(key, ov[kIndex]);
            }
        }

        return res;
    }
    /*
     * @see java.util.SortedMap#headMap(java.lang.Object)
     */
    @Override
    public SortedMap<Boolean, V> headMap (final Boolean toKey)
    {
        return subMap(null, toKey);
    }
    /*
     * @see java.util.SortedMap#tailMap(java.lang.Object)
     */
    @Override
    public SortedMap<Boolean, V> tailMap (final Boolean fromKey)
    {
        return subMap(fromKey, Boolean.TRUE);
    }
}
