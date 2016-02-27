package net.community.chest.ui.helpers.button.group;

import javax.swing.JToggleButton;


/**
 * Copyright 2007 as per GPLv2
 * 
 * @param <B> The reflected {@link JToggleButton} type 
 * @author Lyor G.
 * @since Jul 16, 2007 3:33:41 PM
 */
public class ToggleButtonGroup<B extends JToggleButton> extends TypedButtonGroup<B> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3800406168774560273L;

	public ToggleButtonGroup (Class<B> btnClass)
	{
		super(btnClass);
	}
}
