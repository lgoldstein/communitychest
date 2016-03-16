package net.community.chest.rrd4j.common.graph;

import java.awt.Paint;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.rrd4j.common.core.util.RrdTimestampValueStringInstantiator;
import net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth;
import net.community.chest.util.datetime.DateUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 15, 2008 11:41:50 AM
 */
public class VRuleExt extends AbstractLegendColorWidth {
    private long    _timestamp    /* =0L */;
    public long getTimestamp ()
    {
        return _timestamp;
    }

    public void setTimestamp (long timestamp)
    {
        _timestamp = timestamp;
    }

    public static final String    TIMESTAMP_ATTR="timestamp";
    public Long setTimestamp (Element elem) throws Exception
    {
        final String    val=elem.getAttribute(TIMESTAMP_ATTR);
        final Long        v=((null == val) || (val.length() <= 0)) ? null : RrdTimestampValueStringInstantiator.DEFAULT.newInstance(val);
        if (v != null)
            setTimestamp(v.longValue());
        return v;
    }

    public Element addTimestamp (Element elem) throws Exception
    {
        final long    ts=getTimestamp();
        if (ts > 0L)
            return DOMUtils.addNonEmptyAttribute(elem, TIMESTAMP_ATTR, RrdTimestampValueStringInstantiator.DEFAULT.convertInstance(ts));
        else
            return elem;
    }

    public VRuleExt (String legend, Paint color, float width, long timestamp)
    {
        super(legend, color, width);
        _timestamp = timestamp;
    }

    public VRuleExt ()
    {
        super();
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public VRuleExt fromXml (Element elem) throws Exception
    {
        if (this != super.fromXml(elem))
            throw new IllegalStateException("Mismatched recovered XML instances");

        setTimestamp(elem);
        return this;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    elem=super.toXml(doc);
        addTimestamp(elem);
        return elem;
    }

    public VRuleExt (Element elem) throws Exception
    {
        super(elem);
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.AbstractLegend#getRootElementName()
     */
    @Override
    public String getRootElementName ()
    {
        return RrdGraphDefExt.VRULE_ATTR;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorWidth#toString()
     */
    @Override
    public String toString ()
    {
        return super.toString()
            + "(timestamp=" + DateUtil.parseDateTimeToString(getTimestamp()) + ")"
            ;
    }
}
