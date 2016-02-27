/*
 * 
 */
package net.community.chest.swing.component.text;

import javax.swing.JPasswordField;
import javax.swing.text.Document;

import net.community.chest.CoVariantReturn;
import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Editable;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.FontControl;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.awt.attributes.Tooltiped;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;

import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 11, 2010 7:46:39 AM
 */
public class BasePasswordField extends JPasswordField
			implements XmlConvertible<BasePasswordField>,
					   Textable, Enabled, FontControl, Editable,
					   Tooltiped, Backgrounded, Foregrounded {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5773151541640571316L;

	public BasePasswordField ()
	{
		this((String) null);
	}

	public BasePasswordField (String text)
	{
		this(text, 0);
	}

	public BasePasswordField (int columns)
	{
		this(null, columns);
	}

	public BasePasswordField (String text, int columns)
	{
		this(null, text, columns);
	}

	public BasePasswordField (Document doc, String txt, int columns)
	{
		super(doc, txt, columns);
	}

	protected XmlProxyConvertible<?> getFieldConverter (Element elem)
	{
		return (null == elem) ? null : JPasswordFieldReflectiveProxy.PASSFIELD;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public BasePasswordField fromXml (Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	proxy=getFieldConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					o=
			((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched re-constructed instance");

		return this;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (org.w3c.dom.Document doc) throws Exception
	{
		throw new UnsupportedOperationException("toXml() N/A");
	}

	public BasePasswordField (Element elem) throws Exception
	{
		final Object	o=fromXml(elem);
		if (o != this)
			throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched re-constructed instance");
	}
}
