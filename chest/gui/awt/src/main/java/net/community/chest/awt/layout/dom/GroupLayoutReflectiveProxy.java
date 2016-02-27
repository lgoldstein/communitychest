/*
 * 
 */
package net.community.chest.awt.layout.dom;

import javax.swing.GroupLayout;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @param <L> The reflected {@link GroupLayout}
 * @since Sep 24, 2008 1:54:58 PM
 */
public class GroupLayoutReflectiveProxy<L extends GroupLayout> extends AbstractLayoutManager2ReflectiveProxy<L> {
	public GroupLayoutReflectiveProxy (Class<L> objClass) throws IllegalArgumentException
	{
		super(objClass);
	}

	public static final GroupLayoutReflectiveProxy<GroupLayout>	GROUP=
			new GroupLayoutReflectiveProxy<GroupLayout>(GroupLayout.class);
}
