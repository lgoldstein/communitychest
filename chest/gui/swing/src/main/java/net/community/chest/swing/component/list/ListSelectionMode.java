/*
 * 
 */
package net.community.chest.swing.component.list;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.ListSelectionModel;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to convert between <code>int</code> values and {@link Enum}-s</P>
 * 
 * @author Lyor G.
 * @since Oct 7, 2008 8:45:46 AM
 */
public enum ListSelectionMode {
	SINGLE(ListSelectionModel.SINGLE_SELECTION),
    INTERVAL(ListSelectionModel.SINGLE_INTERVAL_SELECTION),
    MULTIPLE(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

	private final int	_mode;
	public final int getMode ()
	{
		return _mode;
	}

	ListSelectionMode (final int m)
	{
		_mode = m;
	}

	public static final List<ListSelectionMode>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final ListSelectionMode fromString (final String s)
    {
    	return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final ListSelectionMode fromMode (final int m)
    {
    	for (final ListSelectionMode v : VALUES)
    	{
    		if ((v != null) && (v.getMode() == m))
    			return v;
    	}

    	return null;
    }
}
