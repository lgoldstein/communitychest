package net.community.chest.util.map.entries;

import java.util.Map;

import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @param <V> The {@link java.util.Map.Entry} value type
 * @author Lyor G.
 * @since Aug 6, 2007 2:35:17 PM
 */
public class StringMapEntry<V> extends MapEntryImpl<String,V> {
	public StringMapEntry ()
	{
		super();
	}

	public StringMapEntry (Map.Entry<String,? extends V> e)
	{
		super(e);
	}

	public StringMapEntry (String key, V value)
	{
		super(key, value);
	}

	public StringMapEntry (String key)
	{
		super(key);
	}
	/*
	 * @see net.community.chest.util.map.MapEntryImpl#isEmpty()
	 */
	@Override
	public boolean isEmpty ()
	{
		final String	k=getKey();
		final V			v=getValue();
		return ((null == k) || (k.length() <= 0)) && (null == v);
	}
}
