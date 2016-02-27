/*
 * 
 */
package net.community.chest.jfree.jfreechart;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.jfree.jfreechart.axis.AxisLocationValueStringInstantiator;
import net.community.chest.jfree.jfreechart.axis.category.CategoryAnchorValueStringInstantiator;
import net.community.chest.jfree.jfreechart.data.RangeValueStringInstantiator;
import net.community.chest.jfree.jfreechart.plot.DatasetRenderOrderValueStringInstantiator;
import net.community.chest.jfree.jfreechart.plot.PlotOrientationValueStringInstantiator;
import net.community.chest.jfree.jfreechart.plot.SeriesRenderOrderValueStringInstantiator;
import net.community.chest.jfree.jfreechart.plot.dial.DialShapeValueStringInstantiator;
import net.community.chest.reflect.StringInstantiatorsMap;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.data.Range;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 5, 2009 3:19:54 PM
 */
public final class ConvChart {
	private ConvChart ()
	{
		// no instance
	}

	private static ChartInstantiatorsMap	_instMap	/* =null */;
	// CAVEAT EMPTOR
	public static final synchronized ChartInstantiatorsMap getConvertersMap ()
	{
		if (null == _instMap)
		{
			_instMap = new ChartInstantiatorsMap();

			_instMap.put(PlotOrientation.class, PlotOrientationValueStringInstantiator.DEFAULT);
			_instMap.put(AxisLocation.class, AxisLocationValueStringInstantiator.DEFAULT);
			_instMap.put(CategoryAnchor.class, CategoryAnchorValueStringInstantiator.DEFAULT);
			_instMap.put(DatasetRenderingOrder.class, DatasetRenderOrderValueStringInstantiator.DEFAULT);
			_instMap.put(SeriesRenderingOrder.class, SeriesRenderOrderValueStringInstantiator.DEFAULT);
			_instMap.put(DialShape.class, DialShapeValueStringInstantiator.DEFAULT);
			_instMap.put(Range.class, RangeValueStringInstantiator.DEFAULT);
		}

		return _instMap;
	}

	public static <V> ValueStringInstantiator<V> getConverter (final Class<V> c)
	{
		if (null == c)
			return null;

		final StringInstantiatorsMap	cMap=getConvertersMap();
		if ((null == cMap) || (cMap.size() <= 0))
			return null;

		synchronized(cMap)
		{
			@SuppressWarnings("unchecked")
			final ValueStringInstantiator<V>	vsi=
				(ValueStringInstantiator<V>) cMap.get(c);
			return vsi;
		}
	}
}
