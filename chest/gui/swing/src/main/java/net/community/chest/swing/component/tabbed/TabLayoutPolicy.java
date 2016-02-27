/*
 * 
 */
package net.community.chest.swing.component.tabbed;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JTabbedPane;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate the {@link JTabbedPane#setTabLayoutPolicy(int)} value as
 * an {@link Enum}</P>
 * 
 * @author Lyor G.
 * @since Dec 23, 2008 8:42:51 AM
 */
public enum TabLayoutPolicy {
	WRAP(JTabbedPane.WRAP_TAB_LAYOUT),
    SCROLL(JTabbedPane.SCROLL_TAB_LAYOUT);

	private final int	_p;
	public final int getPolicy ()
	{
		return _p;
	}

	TabLayoutPolicy (int p)
	{
		_p = p;
	}

	public static final List<TabLayoutPolicy>	VALUES=Collections.unmodifiableList(Arrays.asList(values()))	/* =null */;
	public static final TabLayoutPolicy fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final TabLayoutPolicy fromPolicy (final int p)
	{
    	for (final TabLayoutPolicy v : VALUES)
    	{
    		if ((v != null) && (v.getPolicy() == p))
    			return v;
    	}

    	return null;
	}
}
