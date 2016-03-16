package net.community.chest.util.map.entries;

import java.util.Map;

import net.community.chest.util.map.MapEntryImpl;

/**
 * Copyright 2007 as per GPLv2
 *
 * The actual {@link java.util.Map.Entry} implementation
 *
 * @param <V> Type of mapped value
 * @author Lyor G.
 * @since Jun 10, 2007 5:17:33 PM
 */
public final class IntegersMapEntry<V> extends MapEntryImpl<Integer,V> {
    public IntegersMapEntry (final Integer k, final V v)
    {
        super(k, v);

        if (null == k)
            throw new NullPointerException("Not allowed null key value");
        if (Integer.MAX_VALUE == k.intValue())
            throw new IllegalArgumentException("Not allowed key=" + k + " value");
        if (null == v)
            throw new NullPointerException("Not allowed null value instance");
    }

    public IntegersMapEntry (final int k, final V v)
    {
        this(Integer.valueOf(k), v);
    }

    public IntegersMapEntry (final Map.Entry<Integer,? extends V> e)
    {
        this((null == e) ? null : e.getKey(), (null == e) ? null : e.getValue());
    }
}
