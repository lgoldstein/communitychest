/*
 * 
 */
package net.community.chest.swing.component.label;

import javax.swing.Icon;
import javax.swing.JLabel;

import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.swing.component.scroll.HorizontalPolicy;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.component.scroll.VerticalPolicy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <C> Type of {@link JLabel} being scrolled
 * @author Lyor G.
 * @since Apr 1, 2009 9:43:14 AM
 */
public class ScrollableLabel<C extends JLabel> extends ScrolledComponent<C>
		// NOTE !!! we do not declare FontControl, Tooltiped, Backgrounded, etc. since these are implemented by the scroll pane itself
		implements Textable, Iconable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 970917226146274773L;
	public ScrollableLabel (Class<C> vc, C view, VerticalPolicy vp, HorizontalPolicy hp)
	{
		super(vc, view, vp, hp);
	}

	public ScrollableLabel (Class<C> vc, C view)
	{
		this(vc, view, VerticalPolicy.BYNEED, HorizontalPolicy.BYNEED);
	}

	public ScrollableLabel (Class<C> vc, VerticalPolicy vp, HorizontalPolicy hp)
	{
		this(vc, null, vp, hp);
	}

	public ScrollableLabel (Class<C> vc)
	{
		this(vc, null);
	}

	@SuppressWarnings("unchecked")
	public ScrollableLabel (C view, VerticalPolicy vp, HorizontalPolicy hp)
	{
		this((null == view) ? null : (Class<C>) view.getClass(), vp, hp);
	}

	public ScrollableLabel (C view)
	{
		this(view, VerticalPolicy.BYNEED, HorizontalPolicy.BYNEED);
	}
	/*
	 * @see net.community.chest.awt.attributes.Iconable#getIcon()
	 */
	@Override
	public Icon getIcon ()
	{
		final JLabel	l=getAssignedValue();
		return (null == l) ? null : l.getIcon();
	}
	/*
	 * @see net.community.chest.awt.attributes.Iconable#setIcon(javax.swing.Icon)
	 */
	@Override
	public void setIcon (Icon i)
	{
		final JLabel	l=getAssignedValue();
		if (l != null)
			l.setIcon(i);
	}
	/*
	 * @see net.community.chest.awt.attributes.Textable#getText()
	 */
	@Override
	public String getText ()
	{
		final JLabel	l=getAssignedValue();
		return (null == l) ? null : l.getText();
	}
	/*
	 * @see net.community.chest.awt.attributes.Textable#setText(java.lang.String)
	 */
	@Override
	public void setText (String t)
	{
		final JLabel	l=getAssignedValue();
		if (l != null)
			l.setText(t);
	}

}
