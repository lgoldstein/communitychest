/*
 * 
 */
package net.community.chest.util.datetime;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates the {@link DateFormat} styles</P>
 * @author Lyor G.
 * @since Jan 27, 2009 3:56:51 PM
 */
public enum DateFormatStyle {
    FULL(DateFormat.FULL),
    LONG(DateFormat.LONG),
    MEDIUM(DateFormat.MEDIUM),
    SHORT(DateFormat.SHORT),
    DEFAULT(DateFormat.DEFAULT);

	private final int	_style;
	public final int getStyle ()
	{
		return _style;
	}

	DateFormatStyle (int s)
	{
		_style = s;
	}

	public static final List<DateFormatStyle>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final DateFormatStyle fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}

	public static final DateFormatStyle fromStyle (final int s)
	{
		for (final DateFormatStyle v : VALUES)
		{
			if ((v != null) && (v.getStyle() == s))
				return v;
		}

		return null;
	}
}
