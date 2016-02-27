package net.community.chest.swing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingConstants;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates vertical alignment values</P>
 * @author Lyor G.
 * @since Mar 24, 2008 11:14:05 AM
 */
public enum VAlignmentValue {
	TOP(SwingConstants.TOP),
    CENTER(SwingConstants.CENTER),
    BOTTOM(SwingConstants.BOTTOM);

	private final int	_alignValue;
	public final int getAlignmentValue ()
	{
		return _alignValue;
	}

	VAlignmentValue (final int alignValue)
	{
		_alignValue = alignValue;
	}

	public static final List<VAlignmentValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final VAlignmentValue fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final VAlignmentValue fromAlignmentValue (final int av)
	{
		for (final VAlignmentValue v : VALUES)
		{
			if ((v != null) && (v.getAlignmentValue() == av))
				return v;
		}

		return null;
	}
}
