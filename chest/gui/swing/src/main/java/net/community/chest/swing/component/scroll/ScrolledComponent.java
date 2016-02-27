/*
 * 
 */
package net.community.chest.swing.component.scroll;

import java.awt.Component;

import javax.swing.JViewport;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.lang.TypedValuesContainer;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <C> Type of component being scrolled
 * @author Lyor G.
 * @since Apr 1, 2009 9:06:53 AM
 */
public class ScrolledComponent<C extends Component> extends BaseScrollPane
		implements TypedComponentAssignment<C>, TypedValuesContainer<C> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4651145750540212034L;
	private final Class<C>	_valClass;
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final Class<C> getValuesClass ()
	{
		return _valClass;
	}
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#getAssignedValue()
	 */
	@Override
	public C getAssignedValue ()
	{
        final JViewport vp=getViewport();
        final Component	vv=(null == vp) ? null : vp.getView();
        final Class<?>	vc=(null == vv) ? null : vv.getClass();
        if (null == vc)
        	return null;

        final Class<C>	cc=getValuesClass();
        if (!cc.isAssignableFrom(vc))	// debug breakpoint
        	throw new ClassCastException("Scrolled component type mismatch: expect=" + cc.getName() + "/got=" + vc.getName());

        return cc.cast(vv);
	}
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#setAssignedValue(java.lang.Object)
	 */
	@Override
	public void setAssignedValue (C value)
	{
		setViewportView(value);
	}

	public ScrolledComponent (Class<C> vc, C view, VerticalPolicy vp, HorizontalPolicy hp)
	{
		super(view, vp, hp);

		if (null == (_valClass=vc))
			throw new IllegalArgumentException("No component class specified");
	}

	public ScrolledComponent (Class<C> vc, C view)
	{
		this(vc, view, VerticalPolicy.BYNEED, HorizontalPolicy.BYNEED);
	}

	public ScrolledComponent (Class<C> vc, VerticalPolicy vp, HorizontalPolicy hp)
	{
		this(vc, null, vp, hp);
	}

	public ScrolledComponent (Class<C> vc)
	{
		this(vc, VerticalPolicy.BYNEED, HorizontalPolicy.BYNEED);
	}
	// NOTE: causes IllegalArgumentException if null view
	@SuppressWarnings("unchecked")
	public ScrolledComponent (C view, VerticalPolicy vp, HorizontalPolicy hp)
	{
		this((null == view) ? null : (Class<C>) view.getClass(), view, vp, hp);
	}
	
	public ScrolledComponent (C view)
	{
		this(view, VerticalPolicy.BYNEED, HorizontalPolicy.BYNEED);
	}
}
