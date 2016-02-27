/*
 * 
 */
package net.community.chest.util.locale;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> The mapped value type
 * @author Lyor G.
 * @since Dec 16, 2008 10:29:08 AM
 */
public class LocalesMap<V> extends TreeMap<Locale,V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9223328327233689552L;

	public LocalesMap (Comparator<? super Locale> comparator)
	{
		super(comparator);
	}

	public LocalesMap ()
	{
		this(DefaultLocaleComparator.ASCENDING);
	}

	public LocalesMap (Map<? extends Locale,? extends V> m)
	{
		super(m);
	}
}
