/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.plot.DatasetRenderingOrder;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 5, 2009 4:04:20 PM
 */
public enum DatasetRenderOrderValue {
	FORWARD(DatasetRenderingOrder.FORWARD),
	REVERSE(DatasetRenderingOrder.REVERSE);

	private final DatasetRenderingOrder	_o;
	public final DatasetRenderingOrder getOrder ()
	{
		return _o;
	}

	DatasetRenderOrderValue (DatasetRenderingOrder o)
	{
		_o = o;
	}

	public static final List<DatasetRenderOrderValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final DatasetRenderOrderValue fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final DatasetRenderOrderValue fromOrder (final DatasetRenderingOrder o)
	{
		if (null == o)
			return null;

		for (final DatasetRenderOrderValue  v : VALUES)
		{
			if ((v != null) && o.equals(v.getOrder()))
				return v;
		}

		return null;
	}
}
