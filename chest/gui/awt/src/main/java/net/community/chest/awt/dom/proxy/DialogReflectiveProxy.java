package net.community.chest.awt.dom.proxy;

import java.awt.Dialog;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <D> The reflected {@link Dialog} type
 * @author Lyor G.
 * @since Mar 20, 2008 10:10:50 AM
 */
public class DialogReflectiveProxy<D extends Dialog> extends WindowReflectiveProxy<D> {
	public DialogReflectiveProxy (Class<D> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected DialogReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
}
