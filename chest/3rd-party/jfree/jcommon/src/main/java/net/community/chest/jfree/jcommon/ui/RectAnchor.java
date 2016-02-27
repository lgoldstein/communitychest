/*
 * 
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.ui.RectangleAnchor;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 26, 2009 1:03:50 PM
 */
public enum RectAnchor {
	CENTER(RectangleAnchor.CENTER),
	TOP(RectangleAnchor.TOP),
	TOPLEFT(RectangleAnchor.TOP_LEFT),
	TOPRIGHT(RectangleAnchor.TOP_RIGHT),
	BOTTOM(RectangleAnchor.BOTTOM),
	BOTTOMLEFT(RectangleAnchor.BOTTOM_LEFT),
	BOTTOMRIGHT(RectangleAnchor.BOTTOM_RIGHT),
	LEFT(RectangleAnchor.LEFT),
	RIGHT(RectangleAnchor.RIGHT);

	private final RectangleAnchor	_a;
	public final RectangleAnchor getAnchor ()
	{
		return _a;
	}

	RectAnchor (RectangleAnchor a)
	{
		_a = a;
	}

	public static final List<RectAnchor>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final RectAnchor fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final RectAnchor fromAnchor (final RectangleAnchor a)
	{
		if (null == a)
			return null;

		for (final RectAnchor  v : VALUES)
		{
			if ((v != null) && a.equals(v.getAnchor()))
				return v;
		}

		return null;
	}
}
