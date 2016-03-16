package net.community.chest.rrd4j.common.graph;

import java.awt.Paint;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 14, 2008 1:59:23 PM
 */
public class LineExt extends AbstractLegendColorWidth {
    public static final String    SRC_NAME_ATTR="srcName";
    private String    _srcName    /* =null */;
    public String getSrcName ()
    {
        return _srcName;
    }

    public void setSrcName (String srcName)
    {
        _srcName = srcName;
    }

    public String setSrcName (Element elem)
    {
        final String    val=elem.getAttribute(SRC_NAME_ATTR);
        if ((val != null) && (val.length() > 0))
            setSrcName(val);
        return val;
    }

    public Element addSrcName (Element elem)
    {
        return DOMUtils.addNonEmptyAttribute(elem, SRC_NAME_ATTR, getSrcName());
    }

    public LineExt (String legend, Paint color, float width, String srcName)
    {
        super(legend, color, width);
        _srcName = srcName;
    }

    public LineExt ()
    {
        super();
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.AbstractLegendAndColor#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public LineExt fromXml (Element elem) throws Exception
    {
        if (this != super.fromXml(elem))
            throw new IllegalStateException("Mismatched recovered XML instances");

        setSrcName(elem);
        return this;
    }

    public LineExt (Element elem) throws Exception
    {
        super(elem);
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.AbstractLegend#getRootElementName()
     */
    @Override
    public String getRootElementName ()
    {
        return RrdGraphDefExt.LINE_ATTR;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    elem=super.toXml(doc);
        addSrcName(elem);

        return elem;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth#clone()
     */
    @Override
    @CoVariantReturn
    public LineExt clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth#toString()
     */
    @Override
    public String toString ()
    {
        return super.toString()
            + "(src=" + getSrcName() + ")"
            ;
    }
}
