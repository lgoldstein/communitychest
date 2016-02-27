/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.axis.DateTickMarkPosition;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulates the {@link DateTickMarkPosition} values as {@link Enum}-s
 * @author Lyor G.
 * @since May 5, 2009 3:24:28 PM
 */
public enum DateTickMarkPosType {
    START(DateTickMarkPosition.START),
    MIDDLE(DateTickMarkPosition.MIDDLE),
    END(DateTickMarkPosition.END);

    private final DateTickMarkPosition	_p;
	public final DateTickMarkPosition getPosition ()
	{
		return _p;
	}
	
	DateTickMarkPosType (DateTickMarkPosition p)
	{
		_p = p;
	}

	public static final List<DateTickMarkPosType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final DateTickMarkPosType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final DateTickMarkPosType fromPosition (final DateTickMarkPosition p)
	{
		if (null == p)
			return null;

		for (final DateTickMarkPosType v : VALUES)
		{
			final DateTickMarkPosition	vp=(null == v) ? null : v.getPosition();
			if (p.equals(vp))
				return v;
		}

		return null;
	}
}
