/*
 * 
 */
package net.community.chest.swing.component.label;

import javax.swing.JLabel;

import net.community.chest.swing.component.scroll.HorizontalPolicy;
import net.community.chest.swing.component.scroll.VerticalPolicy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 1, 2009 9:47:36 AM
 */
public class DefaultLabelScroll extends ScrollableLabel<JLabel> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6722283043993198103L;

	public DefaultLabelScroll (JLabel view, VerticalPolicy vp, HorizontalPolicy hp)
	{
		super(JLabel.class, view, vp, hp);
	}

	public DefaultLabelScroll (VerticalPolicy vp, HorizontalPolicy hp)
	{
		this(null, vp, hp);
	}

	public DefaultLabelScroll (JLabel view)
	{
		this(view, VerticalPolicy.BYNEED, HorizontalPolicy.BYNEED);
	}

	public DefaultLabelScroll ()
	{
		this(null);
	}
}
