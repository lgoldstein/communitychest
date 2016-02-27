package net.community.chest.rrd4j.common.graph.helpers;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.PubliclyCloneable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 16, 2008 2:07:41 PM
 */
public abstract class AbstractLegend implements XmlConvertible<AbstractLegend>, PubliclyCloneable<AbstractLegend> {
	protected AbstractLegend ()
	{
		super();
	}

	private String	_legend		/* =null */;
	public String getLegend ()
	{
		return _legend;
	}

	public void setLegend (String legend)
	{
		_legend = legend;
	}

	protected AbstractLegend (String legend)
	{
		_legend = legend;
	}

	public static final String	LEGEND_ATTR="legend";
	public String setLegend (Element elem)
	{
		final String	val=elem.getAttribute(LEGEND_ATTR);
		if ((val != null) && (val.length() > 0))
			setLegend(val);
		return val;
	}

	public Element addLegend (Element elem)
	{
		return DOMUtils.addNonEmptyAttribute(elem, LEGEND_ATTR, getLegend());
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public AbstractLegend clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@CoVariantReturn
	public AbstractLegend fromXml (Element elem) throws Exception
	{
		setLegend(elem);
		return this;
	}

	protected AbstractLegend (Element elem) throws Exception
	{
		if (this != fromXml(elem))
			throw new IllegalStateException("Mismatched re-constructed instances");
	}

	public abstract String getRootElementName ();
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	public Element toXml (Document doc) throws Exception
	{
		final Element	elem=doc.createElement(getRootElementName());
		addLegend(elem);
		return elem;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return getRootElementName()
			+ "[" + getLegend() + "]"
			;
	}
}
