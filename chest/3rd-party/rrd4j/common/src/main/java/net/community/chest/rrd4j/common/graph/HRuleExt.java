package net.community.chest.rrd4j.common.graph;

import java.awt.Paint;

import net.community.chest.CoVariantReturn;
import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.dom.DOMUtils;
import net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 15, 2008 11:07:34 AM
 */
public class HRuleExt extends AbstractLegendColorWidth {
    private double    _value    /* =0 */;
    public double getValue ()
    {
        return _value;
    }

    public void setValue (double value)
    {
        _value = value;
    }

    public static final String    VALUE_ATTR="value";
    public Double setValue (Element elem) throws Exception
    {
        final String    val=elem.getAttribute(VALUE_ATTR);
        final Double    v=((null == val) || (val.length() <= 0)) ? null : DoubleValueStringConstructor.DEFAULT.newInstance(val);
        if (v != null)
            setValue(v.doubleValue());

        return v;
    }

    public Element addValue (Element elem) throws Exception
    {
        final String    val=DoubleValueStringConstructor.DEFAULT.convertInstance(getValue());
        return DOMUtils.addNonEmptyAttribute(elem, VALUE_ATTR, val);
    }

    public HRuleExt (String legend, Paint color, float width, double value)
    {
        super(legend, color, width);
        _value = value;
    }

    public HRuleExt ()
    {
        super();
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth#clone()
     */
    @Override
    @CoVariantReturn
    public HRuleExt clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public HRuleExt fromXml (Element elem) throws Exception
    {
        if (this != super.fromXml(elem))
            throw new IllegalStateException("Mismatched recovered XML instances");

        setValue(elem);
        return this;
    }

    public HRuleExt (Element elem) throws Exception
    {
        super(elem);
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.AbstractLegend#getRootElementName()
     */
    @Override
    public String getRootElementName ()
    {
        return RrdGraphDefExt.HRULE_ATTR;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    elem=super.toXml(doc);
        addValue(elem);
        return elem;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth#toString()
     */
    @Override
    public String toString ()
    {
        return super.toString()
            + "(value=" + getValue() + ")"
            ;
    }
}
