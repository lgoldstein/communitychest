package net.community.chest.awt.layout.gridbag;

import net.community.chest.reflect.EnumStringInstantiator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 4, 2007 1:59:53 PM
 */
public class GridBagFillValueStringInstantiator extends EnumStringInstantiator<GridBagFillType> {
	public GridBagFillValueStringInstantiator ()
	{
		super(GridBagFillType.class, false);
		setValues(GridBagFillType.VALUES);
	}

	public static final GridBagFillValueStringInstantiator	DEFAULT=new GridBagFillValueStringInstantiator();
}
