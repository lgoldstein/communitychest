/*
 * 
 */
package net.community.apps.common.test.chart;

import java.util.Map;

import net.community.chest.jfree.jfreechart.plot.thermometer.ThermometerSubRangeValue;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 21, 2010 3:24:24 PM
 */
public class HRThermometerPlot extends EntryThermometerPlot {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5815012372503943805L;

	public static final Map.Entry<Number,Number> RANGE=
		new MapEntryImpl<Number,Number>(Integer.valueOf(30), Integer.valueOf(220));

	public static final Map.Entry<Number,Number>	NORMAL_RANGE=
		new MapEntryImpl<Number,Number>(Integer.valueOf(40), Integer.valueOf(75)),
													WARNING_RANGE=
		new MapEntryImpl<Number,Number>(Integer.valueOf(NORMAL_RANGE.getValue().intValue() + 1),
										Integer.valueOf(80)),
													CRITICAL_RANGE=
		new MapEntryImpl<Number,Number>(Integer.valueOf(WARNING_RANGE.getValue().intValue() + 1),
										RANGE.getValue());

	public HRThermometerPlot ()
	{
		super(TimeSeriesTypeCase.HR);
	}
	/*
	 * @see net.community.apps.common.test.chart.EntryThermometerPlot#getRangeBoundaries()
	 */
	@Override
	public final Map.Entry<Number,Number> getRangeBoundaries ()
	{
		return RANGE;
	}
	/*
	 * @see net.community.apps.common.test.chart.EntryThermometerPlot#getSubRangeBoundaries(net.community.chest.jfree.jfreechart.plot.thermometer.ThermometerSubRangeValue)
	 */
	@Override
	public Map.Entry<Number,Number> getSubRangeBoundaries (ThermometerSubRangeValue subType)
	{
		if (null == subType)
			return null;

		switch(subType)
		{
			case CRITICAL	: return CRITICAL_RANGE;
			case WARNING	: return WARNING_RANGE;
			case NORMAL		: return NORMAL_RANGE;
			default			:
				throw new IllegalArgumentException("getSubRangeBoundaries(" + subType + ") unknown range");
		}
	}
}
