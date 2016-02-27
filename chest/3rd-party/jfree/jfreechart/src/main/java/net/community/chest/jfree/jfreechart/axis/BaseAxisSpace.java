/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;

import org.jfree.chart.axis.AxisSpace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 8, 2009 10:02:11 AM
 */
public class BaseAxisSpace extends AxisSpace implements XmlConvertible<BaseAxisSpace> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5290573761488620755L;
	public BaseAxisSpace ()
	{
		super();
	}

	public XmlProxyConvertible<? extends AxisSpace> getAxisSpaceConverter (Element elem)
	{
		return (null == elem) ? null : AxisSpaceReflectiveProxy.AXISSPACE;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseAxisSpace fromXml (Element elem) throws Exception
	{
		final XmlProxyConvertible<? extends AxisSpace>	proxy=getAxisSpaceConverter(elem);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Object									o=
			((XmlProxyConvertible) proxy).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");

		return this;
	}

	public BaseAxisSpace (Element elem) throws Exception
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
