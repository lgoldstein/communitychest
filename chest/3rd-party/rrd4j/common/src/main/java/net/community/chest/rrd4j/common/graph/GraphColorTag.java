package net.community.chest.rrd4j.common.graph;

import net.community.chest.lang.EnumUtil;

import org.rrd4j.graph.RrdGraphConstants;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates the index in {@link org.rrd4j.graph.RrdGraphDef#setColor(int, java.awt.Paint)}</P>
 * @author Lyor G.
 * @since Jan 14, 2008 1:29:57 PM
 */
public enum GraphColorTag implements RrdGraphConstants {
	CANVAS(COLOR_CANVAS),
	BLACK(COLOR_BACK),
	SHADEA(COLOR_SHADEA),
	SHADEB(COLOR_SHADEB),
	GRID(COLOR_GRID),
	MGRID(COLOR_MGRID),
	FONT(COLOR_FONT),
	FRAME(COLOR_FRAME),
	ARROW(COLOR_ARROW);

	private final int	_tagValue;
	public int getTagValue ()
	{
		return _tagValue;
	}

	public String getTagName ()
	{
		return COLOR_NAMES[getTagValue()];
	}

	GraphColorTag (int tagValue)
	{
		_tagValue = tagValue;
	}

	private static GraphColorTag[]	_values	/* =null */;
	public static synchronized GraphColorTag[] getValues ()
	{
		if (null == _values)
			_values = values();
		return _values;
	}

	public static GraphColorTag fromString (String s)
	{
		return EnumUtil.fromString(getValues(), s, false);
	}

	public static GraphColorTag fromTagName (final String s)
	{
		if ((null == s) || (s.length() <= 0))
			return null;

		final GraphColorTag[]	vals=getValues();
		if ((null == vals) || (vals.length <= 0))
			return null;	// should not happen

		for (final GraphColorTag v : vals)
		{
			if ((v != null) && s.equalsIgnoreCase(v.getTagName()))
				return v;
		}

		return null;
	}
}
