/*
 * 
 */
package net.community.chest.awt.layout.border;

import java.awt.BorderLayout;

import net.community.chest.awt.layout.dom.AbstractLayoutManager2ReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <L> The {@link BorderLayout} being reflected
 * @author Lyor G.
 * @since Aug 20, 2008 1:12:50 PM
 */
public class BorderLayoutReflectiveProxy<L extends BorderLayout> extends AbstractLayoutManager2ReflectiveProxy<L> {
	public BorderLayoutReflectiveProxy (Class<L> objClass) throws IllegalArgumentException
	{
		super(objClass);
	}

	public static final BorderLayoutReflectiveProxy<BorderLayout>	BORDER=
			new BorderLayoutReflectiveProxy<BorderLayout>(BorderLayout.class);
}
