package net.community.chest.awt.layout.gridbag;

import java.awt.GridBagConstraints;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Special {@link Enum}-eration type for {@link GridBagConstraints}
 * <I>gridwidth/gridheight</I> members that are non-numerical</P>
 * 
 * @author Lyor G.
 * @since Aug 7, 2007 1:17:16 PM
 */
public enum GridBagGridSizingType {
	RELATIVE(GridBagConstraints.RELATIVE),
	REMAINDER(GridBagConstraints.REMAINDER);

	private final int	_specVal;
	public int getSpecValue ()
	{
		return _specVal;
	}

	GridBagGridSizingType (final int specVal)
	{
		_specVal = specVal;
	}

	public static final List<GridBagGridSizingType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static GridBagGridSizingType fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}

	public static GridBagGridSizingType fromSpecValue (final int specVal)
	{
		for (final GridBagGridSizingType v : VALUES)
		{
			if ((v != null) && (v.getSpecValue() == specVal))
				return v;
		}

		return null;
	}
}
