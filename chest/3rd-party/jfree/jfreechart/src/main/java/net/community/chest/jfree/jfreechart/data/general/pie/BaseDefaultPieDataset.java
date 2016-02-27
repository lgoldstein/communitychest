/*
 * 
 */
package net.community.chest.jfree.jfreechart.data.general.pie;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;

import org.jfree.data.KeyedValues;
import org.jfree.data.general.DefaultPieDataset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 1, 2009 2:07:42 PM
 */
public class BaseDefaultPieDataset extends DefaultPieDataset implements XmlConvertible<BaseDefaultPieDataset> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5175384527812576752L;
	public BaseDefaultPieDataset ()
	{
		super();
	}

	public BaseDefaultPieDataset (KeyedValues data)
	{
		super(data);
	}

	public XmlProxyConvertible<? extends DefaultPieDataset> getDatasetConverter (Element elem)
	{
		return (null == elem) ? null : DefaultPieDatasetReflectiveProxy.DEFPIEDS;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseDefaultPieDataset fromXml (Element elem) throws Exception
	{
		final XmlProxyConvertible<? extends DefaultPieDataset>	proxy=getDatasetConverter(elem);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Object											o=
			(null == elem) ? this : ((XmlProxyConvertible) proxy).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");

		return this;
	}

	public BaseDefaultPieDataset (Element elem) throws Exception
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
