/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot.compass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.needle.ArrowNeedle;
import org.jfree.chart.needle.LineNeedle;
import org.jfree.chart.needle.LongNeedle;
import org.jfree.chart.needle.MeterNeedle;
import org.jfree.chart.needle.MiddlePinNeedle;
import org.jfree.chart.needle.PinNeedle;
import org.jfree.chart.needle.PlumNeedle;
import org.jfree.chart.needle.PointerNeedle;
import org.jfree.chart.needle.ShipNeedle;
import org.jfree.chart.needle.WindNeedle;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 16, 2009 2:13:28 PM
 */
public enum NeedleType {
	ARROW(ArrowNeedle.class, 0),
	LINE(LineNeedle.class, 1),
	LONG(LongNeedle.class, 2),
	PIN(PinNeedle.class, 3),
	PLUM(PlumNeedle.class, 4),
	POINTER(PointerNeedle.class, 5),
	SHIP(ShipNeedle.class, 6),
	WIND(WindNeedle.class, 7),
	// NOTE !!! Arrow is repeated as "8"
	ARROW8(ArrowNeedle.class, 8),
	MIDDLEPIN(MiddlePinNeedle.class, 9);

	private final Class<? extends MeterNeedle>	_ndlClass;
	public final Class<? extends MeterNeedle> getNeedleClass ()
	{
		return _ndlClass;
	}

	private final int	_ov;
	public final int getOrderValue ()
	{
		return _ov;
	}

	NeedleType (Class<? extends MeterNeedle> c, int orderValue)
	{
		_ndlClass = c;
		_ov = orderValue;
	}

	public static final List<NeedleType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final NeedleType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final NeedleType fromClass (final Class<?> c)
	{
		if ((null == c) || (!MeterNeedle.class.isAssignableFrom(c)))
			return null;

		for (final NeedleType v : VALUES)
		{
			final Class<?>	t=(null == v) ? null : v.getNeedleClass();
			if (t == c)
				return v;
		}

		return null;
	}

	public static final NeedleType fromOrderValue (final int ov)
	{
		if (ov < 0)
			return null;

		for (final NeedleType v : VALUES)
		{
			if ((v != null) && (v.getOrderValue() == ov))
				return v;
		}

		return null;
	}
}
