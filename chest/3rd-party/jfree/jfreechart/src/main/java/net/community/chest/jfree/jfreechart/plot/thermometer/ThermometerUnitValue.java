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
 * <P>An {@link Enum} that encapsulates the {@link ThermometerPlot#setUnits(int)} units</P>
 * @author Lyor G.
 * @since Jun 21, 2010 2:07:56 PM
 */
public enum ThermometerUnitValue {
	NONE(ThermometerPlot.UNITS_NONE, '\0'),
	CELSIUS(ThermometerPlot.UNITS_CELCIUS, 'C'),
	KELVIN(ThermometerPlot.UNITS_KELVIN, 'K'),
	FARENHEIT(ThermometerPlot.UNITS_FAHRENHEIT, 'F');

	private final int	_u;
	public final int getUnitValue ()
	{
		return _u;
	}

	public final char	_c;
	public final char getUnitChar ()
	{
		return _c;
	}

	ThermometerUnitValue (final int u, final char c)
	{
		_u = u;
		_c = c;
	}

	public static final List<ThermometerUnitValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final ThermometerUnitValue fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final ThermometerUnitValue fromUnitValue (final int u)
	{
		for (final ThermometerUnitValue v : VALUES)
		{
			if ((v != null) && (v.getUnitValue() == u))
				return v;
		}

		return null;
	}

	public static final ThermometerUnitValue fromUnitChar (final char c)
	{
		final char	vc=Character.toUpperCase(c);
		for (final ThermometerUnitValue v : VALUES)
		{
			if ((v != null) && (v.getUnitChar() == vc))
				return v;
		}

		return null;
	}
}
