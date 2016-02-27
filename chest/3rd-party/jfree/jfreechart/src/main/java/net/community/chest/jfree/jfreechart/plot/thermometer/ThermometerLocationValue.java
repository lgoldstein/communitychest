/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot.thermometer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.plot.ThermometerPlot;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * <P>An {@link Enum} that encapsulates the {@link ThermometerPlot#setAxisLocation(int)}
 * and {@link ThermometerPlot#setValueLocation(int)} arguments</P>
 * 
 * @author Lyor G.
 * @since Jun 21, 2010 2:38:44 PM
 */
public enum ThermometerLocationValue {
    NONE(ThermometerPlot.NONE),
    RIGHT(ThermometerPlot.RIGHT),
    LEFT(ThermometerPlot.LEFT),
    BULB(ThermometerPlot.BULB);

    private final int	_l;
	public final int getLocationValue ()
	{
		return _l;
	}
	
	ThermometerLocationValue (final int l)
	{
		_l = l;
	}

	public static final List<ThermometerLocationValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final ThermometerLocationValue fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final ThermometerLocationValue fromLocationValue (final int l)
	{
		for (final ThermometerLocationValue v : VALUES)
		{
			if ((v != null) && (v.getLocationValue() == l))
				return v;
		}

		return null;
	}
}
