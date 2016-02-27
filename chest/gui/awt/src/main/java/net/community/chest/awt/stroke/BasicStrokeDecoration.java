/*
 * 
 */
package net.community.chest.awt.stroke;

import java.awt.BasicStroke;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate {@link BasicStroke} decoration values to {@link Enum}</P>
 * 
 * @author Lyor G.
 * @since Feb 1, 2009 4:14:31 PM
 */
public enum BasicStrokeDecoration {
	BUTT(BasicStroke.CAP_BUTT),
	ROUND(BasicStroke.CAP_ROUND),
	SQUARE(BasicStroke.CAP_SQUARE);

	private final int	_d;
	public final int getDecoration ()
	{
		return _d;
	}

	BasicStrokeDecoration (int d)
	{
		_d = d;
	}

	public static final List<BasicStrokeDecoration>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final BasicStrokeDecoration fromString (String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final BasicStrokeDecoration fromDecoration (final int d)
	{
		for (final BasicStrokeDecoration v : VALUES)
		{
			if ((v != null) && (v.getDecoration() == d))
				return v;
		}

		return null;
	}
}
