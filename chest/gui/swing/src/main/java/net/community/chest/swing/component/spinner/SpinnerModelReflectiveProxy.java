/*
 * 
 */
package net.community.chest.swing.component.spinner;

import javax.swing.SpinnerModel;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <M> The reflected <@link SpinnerModel>
 * @author Lyor G.
 * @since Oct 15, 2008 3:27:06 PM
 */
public abstract class SpinnerModelReflectiveProxy<M extends SpinnerModel> extends UIReflectiveAttributesProxy<M> {
	protected SpinnerModelReflectiveProxy (Class<M> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected SpinnerModelReflectiveProxy (Class<M> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
}
