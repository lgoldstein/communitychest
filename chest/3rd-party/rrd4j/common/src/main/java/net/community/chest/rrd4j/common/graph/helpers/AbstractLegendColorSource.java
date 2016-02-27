package net.community.chest.rrd4j.common.graph.helpers;

import java.awt.Paint;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 16, 2008 2:29:29 PM
 */
public abstract class AbstractLegendColorSource extends AbstractLegendAndColor {
	public static final String	SRC_NAME_ATTR="srcName";
	private String	_srcName	/* =null */;
	public String getSrcName ()
	{
		return _srcName;
	}

	public void setSrcName (String srcName)
	{
		_srcName = srcName;
	}

	protected AbstractLegendColorSource (String legend, Paint color, String srcName)
	{
		super(legend, color);
		_srcName = srcName;
	}

	public String setSrcName (Element elem)
	{
		final String	val=elem.getAttribute(SRC_NAME_ATTR);
		if ((val != null) && (val.length() > 0))
			setSrcName(val);
		return val;
	}

	public Element addSrcName (Element elem)
	{
		return DOMUtils.addNonEmptyAttribute(elem, SRC_NAME_ATTR, getSrcName());
	}
	
	protected AbstractLegendColorSource ()
	{
		super();
	}

	protected AbstractLegendColorSource (String legend, Paint color)
	{
		this(legend, color, null);
	}

	protected AbstractLegendColorSource (String legend)
	{
		this(legend, null);
	}
	/*
	 * @see net.community.chest.rrd4j.common.graph.AbstractLegendAndColor#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public AbstractLegendColorSource fromXml (Element elem) throws Exception
	{
		if (this != super.fromXml(elem))
			throw new IllegalStateException("Mismatched recovered XML instances");

		setSrcName(elem);
		return this;
	}

	protected AbstractLegendColorSource (Element elem) throws Exception
	{
		super(elem);
	}
	/*
	 * @see net.community.chest.rrd4j.common.graph.AbstractLegendAndColor#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		final Element	elem=super.toXml(doc);
		addSrcName(elem);

		return elem;
	}
	/*
	 * @see net.community.chest.rrd4j.common.graph.AbstractLegendAndColor#clone()
	 */
	@Override
	@CoVariantReturn
	public AbstractLegendColorSource clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegendAndColor#toString()
	 */
	@Override
	public String toString ()
	{
		return super.toString()
			+ "(src=" + getSrcName() + ")"
			;
	}
}
