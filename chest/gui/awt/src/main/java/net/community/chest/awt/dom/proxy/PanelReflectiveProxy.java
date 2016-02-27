/*
 * 
 */
package net.community.chest.awt.dom.proxy;

import java.awt.Panel;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @param <P> The reflected {@link Panel} type
 * @since Feb 16, 2009 12:51:54 PM
 */
public class PanelReflectiveProxy<P extends Panel> extends ContainerReflectiveProxy<P> {
	protected PanelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
			throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public PanelReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}
}
