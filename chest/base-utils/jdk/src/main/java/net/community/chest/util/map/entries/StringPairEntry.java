package net.community.chest.util.map.entries;

import java.util.Map;

import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>{@link java.util.Map.Entry} implementation where key and value are
 * both {@link String}-s</P>
 * 
 * @author Lyor G.
 * @since Aug 6, 2007 2:36:25 PM
 */
public class StringPairEntry extends StringMapEntry<String> implements Comparable<Map.Entry<String,String>> {
	public StringPairEntry ()
	{
		super();
	}

	public StringPairEntry (Map.Entry<String,String> e)
	{
		super(e);
	}

	public StringPairEntry (String key, String value)
	{
		super(key, value);
	}

	public StringPairEntry (String key)
	{
		super(key);
	}
	/*
	 * @see net.community.chest.util.map.entries.StringMapEntry#isEmpty()
	 */
	@Override
	public boolean isEmpty ()
	{
		final String	k=getKey(), v=getValue();
		return ((null == k) || (k.length() <= 0))
			&& ((null == v) || (v.length() <= 0))
			;
	}
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (Map.Entry<String,String> o)
	{
		if (null == o)
			return (-1);

		int	nRes=AbstractComparator.compareComparables(getKey(), o.getKey());
		if (0 == nRes)
			nRes = AbstractComparator.compareComparables(getValue(), o.getValue());
		return nRes;
	}
}
