/*
 * 
 */
package net.community.chest.swing.component.table;

import javax.swing.table.DefaultTableCellRenderer;

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
import net.community.chest.swing.component.label.JLabelReflectiveProxy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 7:54:48 AM
 */
public class BaseTableCellRenderer extends DefaultTableCellRenderer
		implements XmlConvertible<BaseTableCellRenderer>,
				   Iconable, Textable, Tooltiped,
		   		   FontControl, Backgrounded, Foregrounded, Enabled {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1978614334105393542L;
	public BaseTableCellRenderer ()
	{
		super();
	}

	public XmlProxyConvertible<?> getRendererConverter (Element elem)
	{
		return (null == elem) ? null : JLabelReflectiveProxy.LABEL;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseTableCellRenderer fromXml (Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	proxy=getRendererConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					o=
			((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");
		return this;
	}

	public BaseTableCellRenderer (Element elem) throws Exception
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
