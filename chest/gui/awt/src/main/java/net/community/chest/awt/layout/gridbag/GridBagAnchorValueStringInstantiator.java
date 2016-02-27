package net.community.chest.awt.layout.gridbag;

import net.community.chest.reflect.EnumStringInstantiator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 4, 2007 1:57:38 PM
 */
public class GridBagAnchorValueStringInstantiator extends EnumStringInstantiator<GridBagAnchorType> {
	public GridBagAnchorValueStringInstantiator ()
	{
		super(GridBagAnchorType.class, false);
		setValues(GridBagAnchorType.VALUES);
	}

	public static final GridBagAnchorValueStringInstantiator	DEFAULT=new GridBagAnchorValueStringInstantiator();
}
