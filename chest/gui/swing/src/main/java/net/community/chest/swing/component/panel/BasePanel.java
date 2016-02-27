/*
 * 
 */
package net.community.chest.swing.component.panel;

import java.awt.Component;
import java.awt.LayoutManager;
import java.util.Locale;

import javax.swing.JPanel;

import net.community.chest.awt.LocalizedComponent;
import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.dom.proxy.ContainerReflectiveProxy;
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
 * @since Aug 20, 2008 12:56:28 PM
 */
public class BasePanel extends JPanel
		implements XmlConvertible<BasePanel>, LocalizedComponent,
					Foregrounded, Backgrounded, Enabled {
	private static final long serialVersionUID = 2794764065653686299L;

	public BasePanel ()
	{
		super();
	}

	public BasePanel (LayoutManager layout)
	{
		super(layout);
	}

	public BasePanel (boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
	}

	public BasePanel (LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
	}

	protected XmlProxyConvertible<?> getPanelConverter (final Element elem)
	{
		return (null == elem) ? null : BasePanelReflectiveProxy.BASEPNL;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BasePanel fromXml (final Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	proxy=getPanelConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					co=
			((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
		if (co != this)
			throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "fromXml") + " mismatched initialization instances");

		return this;
	}

	public BasePanel (final Element elem) throws Exception
	{
		final Object	p=fromXml(elem);
		if (p != this)	// not allowed
			throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched restored " + JPanel.class.getName() + " instances");
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
}
