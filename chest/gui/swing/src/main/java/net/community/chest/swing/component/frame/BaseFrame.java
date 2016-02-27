/*
 * 
 */
package net.community.chest.swing.component.frame;

import java.awt.Component;
import java.awt.FocusTraversalPolicy;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Window;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;

import net.community.chest.CoVariantReturn;
import net.community.chest.awt.LocalizedComponent;
import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Titled;
import net.community.chest.awt.dom.proxy.ContainerReflectiveProxy;
import net.community.chest.awt.focus.ByComponentFocusTraversalPolicy;
import net.community.chest.awt.focus.ByNameFocusTraversalPolicy;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 11, 2008 3:52:43 PM
 */
public class BaseFrame extends JFrame
		implements XmlConvertible<BaseFrame>, LocalizedComponent,
					Titled, Foregrounded, Backgrounded, Enabled {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2623088843557577651L;
	public BaseFrame () throws HeadlessException
	{
		super();
	}

	public BaseFrame (GraphicsConfiguration gc)
	{
		super(gc);
	}

	public BaseFrame (String title) throws HeadlessException
	{
		super(title);
	}

	public BaseFrame (String title, GraphicsConfiguration gc)
	{
		super(title, gc);
	}

	protected XmlProxyConvertible<?> getFrameConverter (final Element elem)
	{
		return (null == elem) ? null : BaseFrameReflectiveProxy.BASEFRM;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public BaseFrame fromXml (Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	proxy=getFrameConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					co=
			((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
		if (co != this)
			throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "fromXml") + " mismatched updated instances");

		return this;
	}

	public BaseFrame (Element elem) throws Exception
	{
		final Object	inst=fromXml(elem);
		if (inst != this)
			throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + "[" + Window.class.getSimpleName() + "] mismatched instances");
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		// TODO implement toXml
		throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
	}

	private Locale	_lcl	/* =null */;
	/*
	 * @see net.community.chest.ui.helpers.LocalizedComponent#getDisplayLocale()
	 */
	@Override
	public synchronized Locale getDisplayLocale ()
	{
		if (null == _lcl)
			_lcl = Locale.getDefault();
		return _lcl;
	}
	/*
	 * @see net.community.chest.ui.helpers.LocalizedComponent#setDisplayLocale(java.util.Locale)
	 */
	@Override
	public synchronized void setDisplayLocale (Locale l)
	{
		if (_lcl != l)	// debug breakpoint
			_lcl = l;
	}

	public <C extends Component> C addConstrainedComponent (C comp, Node constValue)
	{
		return ContainerReflectiveProxy.addConstrainedComponent(this, comp, constValue);
	}

	public FocusTraversalPolicy setFocusTraversalPolicy (List<Component> comps)
	{
		if ((null == comps) || (comps.size() <= 0))
			return getFocusTraversalPolicy();

		final ByComponentFocusTraversalPolicy	p=new ByComponentFocusTraversalPolicy(comps);
		setFocusTraversalPolicy(p);
		return p;
	}

	public FocusTraversalPolicy setFocusTraversalPolicy (Component ... comps)
	{
		return setFocusTraversalPolicy(((null == comps) || (comps.length <= 0)) ? null : Arrays.asList(comps));
	}
	
	public FocusTraversalPolicy setFocusTraversalPolicy (String ... comps)
	{
		if ((null == comps) || (comps.length <= 0))
			return getFocusTraversalPolicy();

		final ByNameFocusTraversalPolicy	p=new ByNameFocusTraversalPolicy(comps);
		setFocusTraversalPolicy(p);
		return p;
	}
}
