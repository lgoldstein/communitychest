/*
 * 
 */
package net.community.chest.ui.helpers.frame;

import java.awt.GraphicsConfiguration;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.community.chest.ui.helpers.HelperUtils;
import net.community.chest.ui.helpers.SettableComponent;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> The expected value type
 * @author Lyor G.
 * @since Jan 1, 2009 12:58:27 PM
 */
public class SettableFrame<V> extends HelperFrame implements SettableComponent<V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4412215904004788444L;
	public SettableFrame (boolean autoInit)
	{
		super(autoInit);
	}

	public SettableFrame (GraphicsConfiguration gc, boolean autoInit)
	{
		super(gc, autoInit);
	}

	public SettableFrame (String title, boolean autoInit)
	{
		super(title, autoInit);
	}

	public SettableFrame (String title, GraphicsConfiguration gc, boolean autoInit)
	{
		super(title, gc, autoInit);
	}

	public SettableFrame (Document doc, boolean autoInit)
	{
		super(doc, autoInit);
	}

	public SettableFrame (GraphicsConfiguration gc, Document doc, boolean autoInit)
	{
		super(gc, doc, autoInit);
	}

	public SettableFrame (GraphicsConfiguration gc, Element elem, boolean autoInit)
	{
		super(gc, elem, autoInit);
	}

	public SettableFrame (Element elem, boolean autoInit)
	{
		super(elem, autoInit);
	}

	public SettableFrame (Element elem)
	{
		this(elem, true);
	}

	public SettableFrame (Document doc)
	{
		this(doc, true);
	}

	public SettableFrame (GraphicsConfiguration gc, Document doc)
	{
		this(gc, doc, true);
	}

	public SettableFrame (GraphicsConfiguration gc, Element elem)
	{
		this(gc, elem, true);
	}

	public SettableFrame ()
	{
		this(true);
	}

	public SettableFrame (GraphicsConfiguration gc)
	{
		this(gc, true);
	}

	public SettableFrame (String title)
	{
		this(title, true);
	}

	public SettableFrame (String title, GraphicsConfiguration gc)
	{
		this(title, gc, true);
	}
	/**
	 * Recursively invokes the {@link SettableComponent} method on all
	 * contained components that implement it.
	 * @param value The value to use if need to call {@link SettableComponent#setContent(Object)}
	 * or {@link SettableComponent#refreshContent(Object)}
	 * @param itemNewState <P>The call to invoke on {@link SettableComponent} interface:</P></BR>
	 * <UL>
	 * 		<LI><code>null</code> - invoke {@link SettableComponent#clearContent()}</LI>
	 * 		<LI><code>TRUE</code> - invoke {@link SettableComponent#setContent(Object)}</LI>
	 * 		<LI><code>FALSE</code> - invoke {@link SettableComponent#refreshContent(Object)}</LI>
	 * </UL>
	 * @return A {@link Collection} of all the {@link SettableComponent}
	 * on which the interface method was invoked - may be null/empty if no
	 * original components to start with or none was a {@link SettableComponent}
	 */
	public Collection<SettableComponent<V>> updateSettableComponents (V value, Boolean itemNewState)
	{
		return HelperUtils.updateSettableComponents(this, value, itemNewState);
	}
	/*
	 * @see net.community.chest.ui.helpers.SettableComponent#clearContent()
	 */
	@Override
	public void clearContent ()
	{
		updateSettableComponents(null, null);
	}
	/*
	 * @see net.community.chest.ui.helpers.SettableComponent#refreshContent(java.lang.Object)
	 */
	@Override
	public void refreshContent (V value)
	{
		updateSettableComponents(value, Boolean.FALSE);
	}
	/*
	 * @see net.community.chest.ui.helpers.SettableComponent#setContent(java.lang.Object)
	 */
	@Override
	public void setContent (V value)
	{
		updateSettableComponents(value, Boolean.TRUE);
	}
}
