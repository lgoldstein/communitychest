/*
 * 
 */
package net.community.chest.swing.component.scroll;

import java.awt.Component;

import javax.swing.JScrollPane;

import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Tooltiped;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 2, 2009 7:37:01 AM
 */
public class BaseScrollPane extends JScrollPane
		implements XmlConvertible<BaseScrollPane>, Tooltiped, Foregrounded, Backgrounded, Enabled {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7551239563340788236L;
	public BaseScrollPane (Component view, int vsbPolicy, int hsbPolicy)
	{
		super(view, vsbPolicy, hsbPolicy);
	}

	public BaseScrollPane (Component view)
	{
        this(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	public BaseScrollPane (int vsbPolicy, int hsbPolicy)
	{
		this(null, vsbPolicy, hsbPolicy);
	}

	public BaseScrollPane (Component view, VerticalPolicy vp, HorizontalPolicy hp)
	{
		this(view, (null == vp) ? (-1) : vp.getPolicy(), (null == hp) ? (-1) : hp.getPolicy());
	}

	public BaseScrollPane (VerticalPolicy vp, HorizontalPolicy hp)
	{
		this(null, vp, hp);
	}

	public BaseScrollPane ()
	{
        this((Component) null);
	}

	public XmlProxyConvertible<?> getScrollPaneConverter (Element elem)
	{
		return (null == elem) ? null : JScrollPaneReflectiveProxy.SCRLPNE;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseScrollPane fromXml (Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	proxy=getScrollPaneConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					o=
			(null == proxy) ? this : ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");

		return this;
	}

	public BaseScrollPane (Component view, Element elem) throws Exception
	{
		this(view);

		final Object	o=fromXml(elem);
		if (o != this)
			throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");
	}

	public BaseScrollPane (Element elem) throws Exception
	{
		this(null, elem);
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		throw new UnsupportedOperationException("toXml() N/A");
	}
}
