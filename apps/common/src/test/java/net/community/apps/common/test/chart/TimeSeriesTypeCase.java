/*
 * 
 */
package net.community.apps.common.test.chart;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.data.time.TimeSeries;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 22, 2010 9:57:49 AM
 */
public enum TimeSeriesTypeCase {
	SIS,
	DIA,
	HR;
	
	public static final List<TimeSeriesTypeCase>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final TimeSeriesTypeCase fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final TimeSeriesTypeCase fromTimeSeriesKey (final Object key)
	{
		final String	ks=(null == key) ? null : key.toString();
		if ((null == ks) || (ks.length() <= 0))
			return null;

		for (final TimeSeriesTypeCase	v : VALUES)
		{
			final String	kv=(null == v) ? null : v.toString();
			if ((kv != null) && (kv.length() > 0)
			 && StringUtil.startsWith(ks, kv, false, false))
				return v;
		}

		return null;
	}
	
	public static final TimeSeriesTypeCase fromTimeSeries (final TimeSeries ts)
	{
		return fromTimeSeriesKey((null == ts) ? null : ts.getKey());
	}
}
