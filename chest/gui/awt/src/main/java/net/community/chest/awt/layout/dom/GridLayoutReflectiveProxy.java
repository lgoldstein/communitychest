/*
 * 
 */
package net.community.chest.awt.layout.dom;

import java.awt.GridLayout;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <L> The {@link GridLayout} being reflected
 * @author Lyor G.
 * @since Aug 20, 2008 1:25:53 PM
 */
public class GridLayoutReflectiveProxy<L extends GridLayout> extends AbstractLayoutManagerReflectiveProxy<L> {
	public GridLayoutReflectiveProxy (Class<L> objClass) throws IllegalArgumentException
	{
		super(objClass);
	}

	public static final GridLayoutReflectiveProxy<GridLayout>	GRID=
				new GridLayoutReflectiveProxy<GridLayout>(GridLayout.class);
}
