package net.community.chest.awt.layout.border;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Provides an enumeration of the available positions for the {@link BorderLayout}
 * string constant
 * 
 * @author Lyor G.
 * @since Jun 18, 2007 4:35:37 PM
 */
public enum BorderLayoutPosition {
	NORTH(BorderLayout.NORTH),
	SOUTH(BorderLayout.SOUTH),
	EAST(BorderLayout.EAST),
	WEST(BorderLayout.WEST),
	CENTER(BorderLayout.CENTER),
	
	BEFORE_FIRST_LINE(BorderLayout.BEFORE_FIRST_LINE),
	AFTER_LAST_LINE(BorderLayout.AFTER_LAST_LINE),
	BEFORE_LINE_BEGINS(BorderLayout.BEFORE_LINE_BEGINS),
	AFTER_LINE_ENDS(BorderLayout.AFTER_LINE_ENDS),

	PAGE_START(BorderLayout.PAGE_START),
	PAGE_END(BorderLayout.PAGE_END),
	LINE_START(BorderLayout.LINE_START),
	LINE_END(BorderLayout.LINE_END);

	private final String	_position;
	public String getPosition ()
	{
		return _position;
	}

	BorderLayoutPosition (String position)
	{
		_position = position;
	}

	public static final List<BorderLayoutPosition>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final BorderLayoutPosition fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final BorderLayoutPosition fromPosition (final String p)
	{
		if ((null == p) || (p.length() <= 0))
			return null;

		for (final BorderLayoutPosition v : VALUES)
		{
			final String	vp=(null == v) ? null : v.getPosition();
			if (p.equalsIgnoreCase(vp))
				return v;
		}

		return null;
	}
}
