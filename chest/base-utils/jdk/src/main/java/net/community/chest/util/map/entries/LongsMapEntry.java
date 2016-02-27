package net.community.chest.util.map.entries;

import java.util.Map.Entry;

import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @param <V> Type of mapped value
 * @author Lyor G.
 * @since Oct 23, 2007 12:10:31 PM
 */
public final class LongsMapEntry<V> extends MapEntryImpl<Long, V> {
	public LongsMapEntry (final Long k, final V v)
	{
		super(k, v);

		if (null == k)
			throw new NullPointerException("Not allowed null key value");
		if (Integer.MAX_VALUE == k.intValue())
			throw new IllegalArgumentException("Not allowed key=" + k + " value");
		if (null == v)
			throw new NullPointerException("Not allowed null value instance");
	}

	public LongsMapEntry (final int k, final V v)
	{
		this(Long.valueOf(k), v);
	}

	public LongsMapEntry (Entry<Long,? extends V> e)
	{
		this((null == e) ? null : e.getKey(), (null == e) ? null : e.getValue());
	}
}
