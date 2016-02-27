/*
 * 
 */
package net.community.chest.swing.component.label;

import javax.swing.Icon;
import javax.swing.JLabel;

import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.FontControl;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.awt.attributes.Tooltiped;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 28, 2009 9:16:47 AM
 */
public class BaseLabel extends JLabel
		implements XmlConvertible<BaseLabel>,
				   Iconable, Textable, Tooltiped,
				   FontControl, Backgrounded, Foregrounded, Enabled {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3220852719937454481L;
	public BaseLabel (String text, Icon icon, int horizontalAlignment)
	{
		super(text, icon, horizontalAlignment);
	}

	public BaseLabel ()
	{
		this("", null, LEADING);
	}

	public BaseLabel (String text)
	{
		this(text, null, LEADING);
	}

	public BaseLabel (Icon image)
	{
		this(null, image, CENTER);
	}

	public BaseLabel (String text, int horizontalAlignment)
	{
		this(text, null, horizontalAlignment);
	}

	public BaseLabel (Icon image, int horizontalAlignment)
	{
        this(null, image, horizontalAlignment);
	}

	public XmlProxyConvertible<?> getLabelConverter (Element elem)
	{
		return (null == elem) ? null : JLabelReflectiveProxy.LABEL;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseLabel fromXml (Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	proxy=getLabelConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					o=
			((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");
		return this;
	}

	public BaseLabel (Element elem) throws Exception
	{
		final Object	o=fromXml(elem);
		if (o != this)
			throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");
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
