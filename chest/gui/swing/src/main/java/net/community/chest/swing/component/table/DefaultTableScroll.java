/*
 * 
 */
package net.community.chest.swing.component.table;

import javax.swing.JTable;

import net.community.chest.swing.component.scroll.HorizontalPolicy;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.component.scroll.VerticalPolicy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 1, 2009 9:31:27 AM
 */
public class DefaultTableScroll extends ScrolledComponent<JTable> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4708794478571367861L;

	public DefaultTableScroll (JTable view, VerticalPolicy vp, HorizontalPolicy hp)
	{
		super(JTable.class, view, vp, hp);
	}

	public DefaultTableScroll (VerticalPolicy vp, HorizontalPolicy hp)
	{
		this(null, vp, hp);
	}

	public DefaultTableScroll (JTable view)
	{
		this(view, VerticalPolicy.BYNEED, HorizontalPolicy.BYNEED);
	}

	public DefaultTableScroll ()
	{
		this(null);
	}
}
