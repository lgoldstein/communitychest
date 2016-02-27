/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.axis.AxisLocation;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 5, 2009 3:47:14 PM
 */
public enum AxisLocationValue {
    TOPLEFT(AxisLocation.TOP_OR_LEFT),
    TOPRIGHT(AxisLocation.TOP_OR_RIGHT),
    BOTLEFT(AxisLocation.BOTTOM_OR_LEFT),
    BOTRIGHT(AxisLocation.BOTTOM_OR_RIGHT);

	private final AxisLocation	_l;
	public final AxisLocation getLocation ()
	{
		return _l;
	}

	AxisLocationValue (AxisLocation l)
	{
		_l = l;
	}

	public static final List<AxisLocationValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final AxisLocationValue fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final AxisLocationValue fromLocation (final AxisLocation l)
	{
		if (null == l)
			return null;

		for (final AxisLocationValue  v : VALUES)
		{
			if ((v != null) && l.equals(v.getLocation()))
				return v;
		}

		return null;
	}
}
