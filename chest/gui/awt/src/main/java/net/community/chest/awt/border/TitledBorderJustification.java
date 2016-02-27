/*
 * 
 */
package net.community.chest.awt.border;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.border.TitledBorder;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to convert between <code>int</code> values and {@link Enum}-s</P>
 * 
 * @author Lyor G.
 * @since Oct 6, 2008 3:56:08 PM
 */
public enum TitledBorderJustification {
	LEFT(TitledBorder.LEFT),
	CENTER(TitledBorder.CENTER),
	RIGHT(TitledBorder.RIGHT),
	LEADING(TitledBorder.LEADING),
	TRAILING(TitledBorder.TRAILING),
	DEFAULT(TitledBorder.DEFAULT_JUSTIFICATION);

	private final int	_just;
    public final int getJustification ()
    {
    	return _just;
    }

    TitledBorderJustification (final int j)
    {
    	_just = j;
    }

    public static final List<TitledBorderJustification>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final TitledBorderJustification fromString (final String s)
    {
    	return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final TitledBorderJustification fromJustification (final int j)
    {
    	for (final TitledBorderJustification v : VALUES)
    	{
    		if ((v != null) && (v.getJustification() == j))
    			return v;
    	}

    	return null;
    }
}
