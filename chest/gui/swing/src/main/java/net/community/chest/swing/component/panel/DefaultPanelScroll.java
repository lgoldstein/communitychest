/*
 * 
 */
package net.community.chest.swing.component.panel;

import javax.swing.JPanel;

import net.community.chest.swing.component.scroll.HorizontalPolicy;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.component.scroll.VerticalPolicy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 1, 2009 9:40:07 AM
 */
public class DefaultPanelScroll extends ScrolledComponent<JPanel> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7270688790677685399L;

	public DefaultPanelScroll (JPanel view, VerticalPolicy vp, HorizontalPolicy hp)
	{
		super(JPanel.class, view, vp, hp);
	}

	public DefaultPanelScroll (VerticalPolicy vp, HorizontalPolicy hp)
	{
		this(null, vp, hp);
	}

	public DefaultPanelScroll (JPanel view)
	{
		this(view, VerticalPolicy.BYNEED, HorizontalPolicy.BYNEED);
	}

	public DefaultPanelScroll ()
	{
		this(null);
	}
}
