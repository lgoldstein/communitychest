/*
 * 
 */
package net.community.chest.ui.components.spinner.margin;

import javax.swing.SpinnerNumberModel;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Useful model for controlling margins expressed as {@link java.awt.Insets}</P>
 * @author Lyor G.
 * @since Mar 11, 2009 9:37:31 AM
 */
public class MarginSpinnerModel extends SpinnerNumberModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3273267477914132237L;

	public MarginSpinnerModel (int maxValue)
	{
		super(Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(maxValue), Integer.valueOf(1));
	}
}
