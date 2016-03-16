package net.community.chest.rrd4j.common.graph.helpers;

import java.awt.Paint;

import net.community.chest.CoVariantReturn;
import net.community.chest.convert.FloatValueStringConstructor;
import net.community.chest.dom.DOMUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 15, 2008 11:35:08 AM
 */
public abstract class AbstractLegendColorWidth extends AbstractLegendAndColor {
    private float    _width    /* =0 */;
    public float getWidth ()
    {
        return _width;
    }

    public void setWidth (float width)
    {
        _width = width;
    }

    protected AbstractLegendColorWidth ()
    {
        _width = 1.0f;
    }

    public static final String    WIDTH_ATTR="width";
    public Float setWidth (Element elem) throws Exception
    {
        final String    val=elem.getAttribute(WIDTH_ATTR);
        final Float        v=((null == val) || (val.length() <= 0)) ? null : FloatValueStringConstructor.DEFAULT.newInstance(val);
        if (v != null)
            setWidth(v.floatValue());
        return v;
    }

    public Element addWidth (Element elem) throws Exception
    {
        final String    val=FloatValueStringConstructor.DEFAULT.convertInstance(getWidth());
        return DOMUtils.addNonEmptyAttribute(elem, WIDTH_ATTR, val);
    }

    protected AbstractLegendColorWidth (String legend, Paint color, float width)
    {
        super(legend, color);
        _width = width;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.AbstractLegendAndColor#clone()
     */
    @Override
    @CoVariantReturn
    public AbstractLegendColorWidth clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.AbstractLegendAndColor#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public AbstractLegendColorWidth fromXml (Element elem) throws Exception
    {
        if (this != super.fromXml(elem))
            throw new IllegalStateException("Mismatched recovered XML instances");

        setWidth(elem);
        return this;
    }

    protected AbstractLegendColorWidth (Element elem) throws Exception
    {
        super(elem);
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.AbstractLegendAndColor#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    elem=super.toXml(doc);

        addWidth(elem);
        return elem;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.AbstractLegendAndColor#toString()
     */
    @Override
    public String toString ()
    {
        return super.toString()
            + "(width=" + getWidth() + ")"
            ;
    }
}
