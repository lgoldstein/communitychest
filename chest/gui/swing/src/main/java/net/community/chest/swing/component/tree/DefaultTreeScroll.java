/*
 * 
 */
package net.community.chest.swing.component.tree;

import javax.swing.JTree;

import net.community.chest.swing.component.scroll.HorizontalPolicy;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.component.scroll.VerticalPolicy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 1, 2009 9:19:27 AM
 */
public class DefaultTreeScroll extends ScrolledComponent<JTree> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -622927607242417036L;

	public DefaultTreeScroll (JTree view, VerticalPolicy vp, HorizontalPolicy hp)
	{
		super(JTree.class, view, vp, hp);
	}
	
	public DefaultTreeScroll (VerticalPolicy vp, HorizontalPolicy hp)
	{
		this(null, vp, hp);
	}

	public DefaultTreeScroll (JTree view)
	{
		this(view, VerticalPolicy.BYNEED, HorizontalPolicy.BYNEED);
	}

	public DefaultTreeScroll ()
	{
		this(null);
	}
}
