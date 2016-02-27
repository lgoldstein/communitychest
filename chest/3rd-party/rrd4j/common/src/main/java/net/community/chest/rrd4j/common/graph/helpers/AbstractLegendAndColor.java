package net.community.chest.rrd4j.common.graph.helpers;

import java.awt.Color;
import java.awt.Paint;

import net.community.chest.CoVariantReturn;
import net.community.chest.awt.dom.converter.ColorValueInstantiator;
import net.community.chest.dom.DOMUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 16, 2008 2:14:51 PM
 */
public abstract class AbstractLegendAndColor extends AbstractLegend {
	protected AbstractLegendAndColor ()
	{
		super();
	}

	private Paint	_color	/* =null */;
	public Paint getColor ()
	{
		return _color;
	}

	public void setColor (Paint color)
	{
		_color = color;
	}

	public static final String	COLOR_ATTR="color";
	public Paint setColor (Element elem) throws Exception
	{
		final String	val=elem.getAttribute(COLOR_ATTR);
		final Paint		v=((null == val) || (val.length() <= 0)) ? null : ColorValueInstantiator.DEFAULT.newInstance(val);
		if (v != null)
			setColor(v);
		return v;
	}

	public Element addColor (Element elem) throws Exception
	{
		final Paint	v=getColor();
		if (null == v)
			return elem;

		if (!(v instanceof Color))
			throw new ClassCastException("Non-" + Color.class.getSimpleName() + " " + Paint.class.getSimpleName() + " value");

		final String	s=ColorValueInstantiator.DEFAULT.convertInstance((Color) v);
		return DOMUtils.addNonEmptyAttribute(elem, COLOR_ATTR, s);
	}

	protected AbstractLegendAndColor (String legend, Paint color)
	{
		super(legend);
		_color = color;
	}

	protected AbstractLegendAndColor (String legend)
	{
		this(legend, null);
	}

	protected AbstractLegendAndColor (Element elem) throws Exception
	{
		super(elem);
	}
	/*
	 * @see net.community.chest.rrd4j.common.graph.AbstractLegend#clone()
	 */
	@Override
	@CoVariantReturn
	public AbstractLegendAndColor clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see net.community.chest.rrd4j.common.graph.AbstractLegend#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public AbstractLegendAndColor fromXml (Element elem) throws Exception
	{
		if (this != super.fromXml(elem))
			throw new IllegalStateException("Mismatched recovered XML instances");

		setColor(elem);
		return this;
	}
	/*
	 * @see net.community.chest.rrd4j.common.graph.AbstractLegend#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		final Element	elem=super.toXml(doc);
		addColor(elem);
		return elem;
	}
	/*
	 * @see net.community.chest.rrd4j.common.graph.AbstractLegend#toString()
	 */
	@Override
	public String toString ()
	{
		return super.toString()
			+ "(color=" + getColor() + ")"
			;
	}
}
