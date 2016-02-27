/*
 * 
 */
package net.community.chest.jfree.jfreechart;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.ChartColor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate the predefined {@link ChartColor}-s as {@link Enum}</P>
 * 
 * @author Lyor G.
 * @since Jan 26, 2009 2:07:17 PM
 */
public enum ChartColors {
	DARK_RED(ChartColor.DARK_RED),
	DARK_BLUE(ChartColor.DARK_BLUE),
	DARK_GREEN(ChartColor.DARK_GREEN),
	DARK_YELLOW(ChartColor.DARK_YELLOW),
	DARK_MAGENTA(ChartColor.DARK_MAGENTA),
	DARK_CYAN(ChartColor.DARK_CYAN),
	LIGHT_RED(ChartColor.LIGHT_RED),
	LIGHT_BLUE(ChartColor.LIGHT_BLUE),
	LIGHT_GREEN(ChartColor.LIGHT_GREEN),
	LIGHT_YELLOW(ChartColor.LIGHT_YELLOW),
	LIGHT_MAGENTA(ChartColor.LIGHT_MAGENTA),
	LIGHT_CYAN(ChartColor.LIGHT_CYAN),
	VERY_DARK_RED(ChartColor.VERY_DARK_RED),
	VERY_DARK_BLUE(ChartColor.VERY_DARK_BLUE),
	VERY_DARK_GREEN(ChartColor.VERY_DARK_GREEN),
	VERY_DARK_YELLOW(ChartColor.VERY_DARK_YELLOW),
	VERY_DARK_MAGENTA(ChartColor.VERY_DARK_MAGENTA),
	VERY_DARK_CYAN(ChartColor.VERY_DARK_CYAN),
	VERY_LIGHT_RED(ChartColor.VERY_LIGHT_RED),
	VERY_LIGHT_BLUE(ChartColor.VERY_LIGHT_BLUE),
	VERY_LIGHT_GREEN(ChartColor.VERY_LIGHT_GREEN),
	VERY_LIGHT_YELLOW(ChartColor.VERY_LIGHT_YELLOW),
	VERY_LIGHT_MAGENTA(ChartColor.VERY_LIGHT_MAGENTA),
	VERY_LIGHT_CYAN(ChartColor.VERY_LIGHT_CYAN);	

    private final Color	_color;
	public Color getChartColor ()
	{
		return _color;
	}

	ChartColors (Color c)
	{
		_color = c;
	}

	public static final List<ChartColors>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	/**
	 * @param s {@link String} containing one of the defined enum values
	 * @return matching enum (case insensitive) - null if no match found
	 * (or if null/empty string to begin with)
	 */
	public static ChartColors fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
	/**
	 * @param c {@link Color} instance
	 * @return matching enum - null if no match (or null {@link Color} to
	 * begin with)
	 */
	public static ChartColors fromColor (final Color c)
	{
		if (null == c)
			return null;

		for (final ChartColors v : VALUES)
		{
			if ((v != null) && c.equals(v.getChartColor()))
				return v;
		}

		return null;	// no match
	}
	/**
	 * @param rgb The RGB value
	 * @return matching enum - null if no match
	 */
	public static ChartColors fromRGB (final int rgb)
	{
		for (final ChartColors v : VALUES)
		{
			final Color	c=v.getChartColor();
			if (c.getRGB() == rgb)
				return v;
		}

		return null;	// no match
	}}
