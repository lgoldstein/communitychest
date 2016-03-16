/*
 *
 */
package net.community.chest.jfree.jfreechart.axis.value;

import java.text.DateFormat;
import java.util.Map;
import java.util.NoSuchElementException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.lang.StringUtil;
import net.community.chest.text.SimpleDateFormatValueStringInstantiator;

import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 5, 2009 3:05:01 PM
 */
public class DateTickUnitConverter implements XmlConvertible<DateTickUnit>, XmlValueInstantiator<DateTickUnit> {
    public DateTickUnitConverter ()
    {
        super();
    }

    public DateTickUnitType getUnit (final Map<String,String> aMap, final String n)
    {
        final String    s=
            ((null == aMap) || (aMap.size() <= 0) || (null == n) || (n.length() <= 0)) ? null : aMap.get(n),
                        vs=
            StringUtil.getCleanStringValue(s);
        if ((null == vs) || (vs.length() <= 0))
            return null;

        final DateTickUnitTypeEnum    t=DateTickUnitTypeEnum.fromString(vs);
        if (null == t)
            throw new NoSuchElementException("getUnit(" + vs + ") unknown unit");

        return t.getUnitType();
    }

    public Integer getCount (final Map<String,String> aMap, final String n)
    {
        final String    s=
            ((null == aMap) || (aMap.size() <= 0) || (null == n) || (n.length() <= 0)) ? null : aMap.get(n),
                        vs=
            StringUtil.getCleanStringValue(s);
        if ((null == vs) || (vs.length() <= 0))
            return null;

        return Integer.decode(vs);
    }

    public DateFormat getFormat (final Map<String,String> aMap, final String n) throws Exception
    {
        final String    s=
            ((null == aMap) || (aMap.size() <= 0) || (null == n) || (n.length() <= 0)) ? null : aMap.get(n),
                        vs=
            StringUtil.getCleanStringValue(s);
        if ((null == vs) || (vs.length() <= 0))
            return null;

        return SimpleDateFormatValueStringInstantiator.DEFAULT.newInstance(vs);
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public DateTickUnit fromXml (Element elem) throws Exception
    {
        final Map<String,String>    aMap=DOMUtils.getNodeAttributes(elem, false);
        if ((null == aMap) || (aMap.size() <= 0))
            return null;

        final DateTickUnitType    u=getUnit(aMap, "unit"),
                                ru=getUnit(aMap, "rollUnit");
        final Integer            c=getCount(aMap, "count"),
                                rc=getCount(aMap, "rollCount");
        final DateFormat        f=getFormat(aMap, "format");
        if ((null == ru) || (null == rc))
            return new DateTickUnit(u, c.intValue(), f);
        else
            return new DateTickUnit(u, c.intValue(), ru, rc.intValue(), f);
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        // TODO implement "toXml"
        throw new UnsupportedOperationException("toXml() N/A");
    }

    public static final DateTickUnitConverter    DEFAULT=new DateTickUnitConverter();
}
