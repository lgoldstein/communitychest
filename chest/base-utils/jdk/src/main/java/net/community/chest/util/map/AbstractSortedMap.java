package net.community.chest.util.map;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedMap;

/**
 * Copyright 2007 as per GPLv2
 *
 * Helper class for {@link SortedMap} implementors - provides some useful
 * common functionality
 *
 * @param <K> Generic key type
 * @param <V> Generic value type
 * @author Lyor G.
 * @since Jun 13, 2007 11:57:53 AM
 */
public abstract class AbstractSortedMap<K,V> extends ExtendedAbstractMap<K,V> implements SortedMap<K,V> {
    protected AbstractSortedMap (Class<K> kClass, Class<V> objClass)
    {
        super(kClass, objClass);
    }
    /**
     * @param fromKey key value to start with (inclusive)
     * @param toKey key value to end (exclusive)
     * @return number of keys in range
     * @throws NullPointerException if either range key is null
     * @throws IllegalStateException if no {@link Comparator} instance
     * returned by call to {@link SortedMap#comparator()} method
     * @throws IllegalArgumentException if range is inverted (i.e., 'from' key
     * is greater than 'to' key)
     */
    public int countKeysInRange (final K fromKey, final K toKey)
        throws NullPointerException, IllegalStateException, IllegalArgumentException
    {
        if ((null == fromKey) || (null == toKey))
            throw new NullPointerException("countKeysInRange() null from(" + fromKey + ")/to(" + toKey + ") key(s)");

        final Comparator<? super K>    c=comparator();
        if (null == c)
            throw new IllegalStateException("countKeysInRange(" + fromKey + " - " + toKey + ") no comparator instance");

        final int    kRes=c.compare(fromKey, toKey);
        if (kRes > 0)
            throw new IllegalArgumentException("countKeysInRange(" + fromKey + " - " + toKey + ") inverted range");
        if (0 == kRes)
            return 0;

        final Collection<? extends K>    ks=keySet();
        if ((null == ks) || (ks.size() <= 0))    // check the obvious
            return 0;

        int    numKeys=0;
        for (final K k : ks)
        {
            // if reached/exceeded top key, stop here
            if (c.compare(k, toKey) >= 0)
                return numKeys;
            // count keys that are greater or equal to the low key
            if (c.compare(fromKey, k) <= 0)
                numKeys++;
        }

        return numKeys;
    }
}
