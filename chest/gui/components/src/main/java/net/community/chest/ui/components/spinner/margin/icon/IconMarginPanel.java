/*
 * 
 */
package net.community.chest.ui.components.spinner.margin.icon;

import javax.swing.JSpinner;

import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.ui.components.spinner.margin.MarginPanel;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 12, 2009 10:06:21 AM
 */
public class IconMarginPanel extends MarginPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6726024247142050994L;
	public IconMarginPanel ()
	{
		super();
	}
	/*
	 * @see net.community.chest.ui.components.spinner.margin.MarginPanel#createSpinner(net.community.chest.awt.layout.BorderLayoutPosition)
	 */
	@Override
	protected JSpinner createSpinner (BorderLayoutPosition p)
	{
		return new IconMarginSpinner(p);
	}
}
