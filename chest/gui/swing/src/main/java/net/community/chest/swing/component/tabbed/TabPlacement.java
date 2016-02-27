/*
 * 
 */
package net.community.chest.swing.component.tabbed;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingConstants;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate the {@link javax.swing.JTabbedPane#setTabPlacement(int)} argument
 * as an {@link Enum}</P>
 * 
 * @author Lyor G.
 * @since Dec 23, 2008 8:35:47 AM
 */
public enum TabPlacement {
	TOP(SwingConstants.TOP), 
	LEFT(SwingConstants.LEFT),
    BOTTOM(SwingConstants.BOTTOM),
    RIGHT(SwingConstants.RIGHT);

	private final int	_p;
	public final int getPlacement ()
	{
		return _p;
	}

	TabPlacement (int p)
	{
		_p = p;
	}

	public static final List<TabPlacement>	VALUES=Collections.unmodifiableList(Arrays.asList(values()))	/* =null */;
	public static final TabPlacement fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final TabPlacement fromPlacement (final int p)
	{
    	for (final TabPlacement v : VALUES)
    	{
    		if ((v != null) && (v.getPlacement() == p))
    			return v;
    	}

    	return null;
	}
}
