/*
 * 
 */
package net.community.chest.swing.component.list;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JList;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to convert between <code>int</code> values and {@link Enum}-s</P>
 *
 * @author Lyor G.
 * @since Oct 7, 2008 8:40:05 AM
 */
public enum ListLayoutOrientation {
	VERTICAL(JList.VERTICAL), 
    HORIZONTAL_WRAP(JList.HORIZONTAL_WRAP),
    VERTICAL_WRAP(JList.VERTICAL_WRAP);

	private final int	_o;
	public final int getOrientation ()
	{
		return _o;
	}

	ListLayoutOrientation (final int o)
	{
		_o = o;
	}

	public static final List<ListLayoutOrientation>	VALUES=Collections.unmodifiableList(Arrays.asList(values()))	/* =null */;
    public static final ListLayoutOrientation fromString (final String s)
    {
    	return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final ListLayoutOrientation fromOrienation (final int o)
    {
    	for (final ListLayoutOrientation v : VALUES)
    	{
    		if ((v != null) && (v.getOrientation() == o))
    			return v;
    	}

    	return null;
    }
}
