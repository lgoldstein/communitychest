/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot.pie;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.plot.PieLabelLinkStyle;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate the {@link PieLabelLinkStyle} as an {@link Enum}</P>
 * @author Lyor G.
 * @since Feb 1, 2009 2:57:43 PM
 */
public enum PieLabelLinkStyleEnum {
	STD(PieLabelLinkStyle.STANDARD),
	QCURVE(PieLabelLinkStyle.QUAD_CURVE),
	CCURVE(PieLabelLinkStyle.CUBIC_CURVE);

	private final PieLabelLinkStyle	_s;
	public final PieLabelLinkStyle getStyle ()
	{
		return _s;
	}
	
	PieLabelLinkStyleEnum (PieLabelLinkStyle s)
	{
		_s = s;
	}

	public static final List<PieLabelLinkStyleEnum>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final PieLabelLinkStyleEnum fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final PieLabelLinkStyleEnum fromStyle (final PieLabelLinkStyle s)
	{
		if (null == s)
			return null;

		for (final PieLabelLinkStyleEnum v : VALUES)
		{
			if ((v != null) && s.equals(v.getStyle()))
				return v;
		}

		return null;
	}
}
