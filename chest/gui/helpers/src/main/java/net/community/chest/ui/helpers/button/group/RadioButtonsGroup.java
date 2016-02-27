package net.community.chest.ui.helpers.button.group;

import javax.swing.JRadioButton;

/**
 * Copyright 2007 as per GPLv2
 * 
 * @param <V> The extended {@link JRadioButton}
 * @author Lyor G.
 * @since Jul 16, 2007 3:30:43 PM
 */
public class RadioButtonsGroup<V extends JRadioButton> extends ToggleButtonGroup<V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1089823056414929376L;

	public RadioButtonsGroup (Class<V> rbClass)
	{
		super(rbClass);
	}

	public static class JRadioButtonGroup extends RadioButtonsGroup<JRadioButton> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2055706920824090918L;

		public JRadioButtonGroup ()
		{
			super(JRadioButton.class);
		}
	}
}
