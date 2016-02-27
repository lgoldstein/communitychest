package net.community.chest.ui.helpers.button.group;

import java.awt.GridLayout;

import javax.swing.JRadioButton;

import net.community.chest.ui.helpers.button.TypedRadioButton;
import net.community.chest.ui.helpers.panel.HelperPanel;

/**
 * Copyright 2007 as per GPLv2
 * 
 * A set of {@link TypedRadioButton}-s grouped together either vertically or
 * horizontally in a {@link GridLayout}. Buttons are displayed according to
 * the order in which they were {@link #addButton(JRadioButton)}-ed
 * 
 * @param <V> The assigned radio button value
 * @param <B>  The {@link TypedRadioButton} type
 * @author Lyor G.
 * @since Jul 16, 2007 3:08:53 PM
 */
public class RadioButtonsPanel<V,B extends TypedRadioButton<V>> extends HelperPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4681383560713083783L;
	private final RadioButtonsGroup<B> _group;
	public B getSelectedButton ()
	{
		 for (final B rb : _group)
		 {
			 if ((rb != null) && rb.isSelected())
				 return rb; 
		 }

		 return null;
	}

	public RadioButtonsPanel (Class<B> bc, boolean vertically, boolean autoLayout)
	{
		super(new GridLayout(vertically ? 0 : 1, vertically ? 1 : 0), false /* no auto-layout till group initialized */);
		_group = new RadioButtonsGroup<B>(bc);

		if (autoLayout)
			layoutComponent();
	}

	public RadioButtonsPanel (Class<B> bc, boolean vertically)
	{
		this(bc, vertically, true);
	}

	public RadioButtonsPanel (Class<B> bc)
	{
		this(bc, true);
	}
	/**
	 * @param btn {@link JRadioButton} to be added - ignored if null.</P>
	 * <B>Note:</B> it is up to the caller to make sure button has all the
	 * relevant action listeners registered to it
	 */
	public void addButton (JRadioButton btn)
	{
		if (btn != null)
		{
			_group.add(btn);
			add(btn);
		}
	}
	// dummy placeholder used for JRadioButtonsPanel
	public static final class VoidTypedRadioButton extends TypedRadioButton<Void> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7543397846927204162L;

		public VoidTypedRadioButton ()
		{
			super(Void.class);
		}
	}

	public static class JRadioButtonsPanel extends RadioButtonsPanel<Void,VoidTypedRadioButton> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6562281806029211281L;

		public JRadioButtonsPanel (boolean vertically, boolean autoLayout)
		{
			super(VoidTypedRadioButton.class, vertically, autoLayout);
		}

		public JRadioButtonsPanel (boolean vertically)
		{
			this(vertically, true);
		}

		public JRadioButtonsPanel ()
		{
			this(true);
		}
	}
}
