package net.community.chest.util.map.entries;

import java.util.Map.Entry;

import net.community.chest.util.map.MapEntryImpl;

/**
 * Copyright 2007 as per GPLv2
 * 
 * @param <V> Type of contained value(s)
 * @author Lyor G.
 * @since Jun 21, 2007 2:30:33 PM
 */
public final class BooleansMapEntry<V> extends MapEntryImpl<Boolean, V> {
	public BooleansMapEntry ()
	{
		super();
	}

	public BooleansMapEntry (Boolean key, V value)
	{
		super(key, value);
	}

	public BooleansMapEntry (Boolean key)
	{
		super(key);
	}

	public BooleansMapEntry (Entry<Boolean,? extends V> e)
	{
		super(e);
	}
}