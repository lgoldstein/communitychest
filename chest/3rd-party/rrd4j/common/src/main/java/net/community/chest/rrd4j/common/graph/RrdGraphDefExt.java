package net.community.chest.rrd4j.common.graph;

import java.awt.Paint;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.rrd4j.common.RrdUtils;
import net.community.chest.rrd4j.common.proxy.RrdGraphDefReflectiveProxy;
import net.community.chest.rrd4j.common.proxy.RrdGraphDefFieldsAccessor;
import net.community.chest.util.datetime.TimeUnits;

import org.rrd4j.graph.RrdGraphDef;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 14, 2008 1:14:37 PM
 */
public class RrdGraphDefExt extends RrdGraphDef implements XmlConvertible<RrdGraphDefExt> {
    public RrdGraphDefExt ()
    {
        super();
    }

    public static final Object getFieldValue (RrdGraphDef g, String name) throws Exception
    {
        return RrdGraphDefFieldsAccessor.DEFAULT.getFieldValue(g, name);
    }

    protected Object getFieldValue (String name) throws RuntimeException
    {
        try
        {
            return getFieldValue(this, name);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }

    protected <T> T getCastFieldValue (String name, Class<T> objClass) throws RuntimeException
    {
        return objClass.cast(getFieldValue(name));
    }

    public static final String    FILENAME_ATTR="filename";
    public String getFilename ()
    {
        return getCastFieldValue(FILENAME_ATTR, String.class);
    }

    public static final String    POOL_USED_ATTR="poolUsed";
    public boolean isPoolUsed ()
    {
        return getCastFieldValue(POOL_USED_ATTR, Boolean.class).booleanValue();
    }

    public static final String    START_TIME_ATTR="startTime";
    public long getStartTime ()
    {
        return getCastFieldValue(START_TIME_ATTR, Long.class).longValue();
    }

    public void setStartTime (Date time)
    {
        setStartTime(RrdUtils.toRrdTime(time));
    }

    public void setStartTime (Calendar time)
    {
        setStartTime(RrdUtils.toRrdTime(time));
    }

    public static final String    END_TIME_ATTR="endTime";
    public long getEndTime ()
    {
        return getCastFieldValue(END_TIME_ATTR, Long.class).longValue();
    }

    public void setEndTime (Date time)
    {
        setEndTime(RrdUtils.toRrdTime(time));
    }

    public void setEndTime (Calendar time)
    {
        setEndTime(RrdUtils.toRrdTime(time));
    }

    public static final String    STEP_ATTR="step";
    public long getStep ()
    {
        return getCastFieldValue(STEP_ATTR, Long.class).longValue();
    }

    public void setStep (TimeUnits u, long stepVal)
    {
        final long    val=u.getMilisecondValue(stepVal);
        setStep(RrdUtils.toRrdTime(val));
    }

    public void setTimeSpan (Date startTime, Date endTime)
    {
        setTimeSpan(RrdUtils.toRrdTime(startTime), RrdUtils.toRrdTime(endTime));
    }

    public void setTimeSpan (Calendar startTime, Calendar endTime)
    {
        setTimeSpan(RrdUtils.toRrdTime(startTime), RrdUtils.toRrdTime(endTime));
    }

    public void setColor (GraphColorTag colorTag, Paint color)
    {
        setColor(colorTag.getTagValue(), color);
    }

    public static final String    VRULE_ATTR="vrule";
    public <VR extends VRuleExt> VR vrule (VR vr)
    {
        if (vr != null)
            vrule(vr.getTimestamp(), vr.getColor(), vr.getLegend(), vr.getWidth());
        return vr;
    }
    public VRuleExt vrule (Element elem) throws Exception
    {
        return vrule(new VRuleExt(elem));
    }

    public static final String    HRULE_ATTR="hrule";
    public <HR extends HRuleExt> HR hrule (HR hr)
    {
        if (hr != null)
            hrule(hr.getValue(), hr.getColor(), hr.getLegend(), hr.getWidth());
        return hr;
    }

    public HRuleExt hrule (Element elem) throws Exception
    {
        return hrule(new HRuleExt(elem));
    }

    public <L extends LineExt> L line (L l)
    {
        if (l != null)
            line(l.getSrcName(), l.getColor(), l.getLegend(), l.getWidth());
        return l;
    }

    public static final String    LINE_ATTR="line";
    public LineExt line (Element elem) throws Exception
    {
        return line(new LineExt(elem));
    }

    public <D extends CDefExt> D datasource (D def)
    {
        if (def != null)
            datasource(def.getName(), def.getRpnExpression());
        return def;
    }

    public <D extends SDefExt> D datasource (D def)
    {
        if (def != null)
            datasource(def.getName(), def.getDefName(), def.getConsolFun());
        return def;
    }

    public <D extends DefExt> D datasource (D def)
    {
        if (def != null)
            datasource(def.getName(), def.getRrdPath(), def.getDsName(), def.getConsolFun(), def.getBackend());
        return def;
    }

    public <S extends SourceExt> S addDatasource (S ds) throws NoSuchElementException
    {
        if (null == ds)
            return ds;

        if (ds instanceof CDefExt)
            datasource((CDefExt) ds);
        else if (ds instanceof SDefExt)
            datasource((SDefExt) ds);
        else if (ds instanceof DefExt)
            datasource((DefExt) ds);
        else
            throw new NoSuchElementException("addDatasource(" + ds + ") unknown class: " + ds.getClass().getName());

        return ds;
    }

    public static final String    DEF_ELEM_NAME="Def",
                                SDEF_ELEM_NAME="SDef",
                                   CDEF_ELEM_NAME="CDef";
    public static final boolean isDefaultDatasourceElementName (String tagName)
    {
        return CDEF_ELEM_NAME.equalsIgnoreCase(tagName)
            || SDEF_ELEM_NAME.equalsIgnoreCase(tagName)
            || DEF_ELEM_NAME.equalsIgnoreCase(tagName)
            ;
    }

    public boolean isDatasourceElementName (String tagName)
    {
        return isDefaultDatasourceElementName(tagName);
    }

    public SourceExt datasource (Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (CDEF_ELEM_NAME.equalsIgnoreCase(tagName))
            return datasource(new CDefExt(elem));
        else if (SDEF_ELEM_NAME.equalsIgnoreCase(tagName))
            return datasource(new SDefExt(elem));
        else if (DEF_ELEM_NAME.equalsIgnoreCase(tagName))
            return datasource(new DefExt(elem));

        throw new NoSuchElementException("datasource(" + tagName + ") unknown data source type");
    }

    public static final String    STACK_ATTR="stack";
    public <S extends StackExt> S stack (S s)
    {
        if (s != null)
            stack(s.getSrcName(), s.getColor(), s.getLegend());
        return s;
    }

    public StackExt stack (Element elem) throws Exception
    {
        return (null == elem) ? null : stack(new StackExt(elem));
    }

    public static final String    AREA_ATTR="area";
    public <A extends AreaExt> A area (A a)
    {
        if (a != null)
            area(a.getSrcName(), a.getColor(), a.getLegend());
        return a;
    }

    public AreaExt area (Element elem) throws Exception
    {
        return (null == elem) ? null : area(new AreaExt(elem));
    }

    public static final String    TIME_AXIS_ATTR="TimeAxis";
    public <A extends TimeAxisExt> A timeAxis (A a)
    {
        if (a != null)
            setTimeAxis(a.getMinorUnit(), a.getMinorUnitCount(), a.getMajorUnit(), a.getMajorUnitCount(), a.getLabelUnit(), a.getLabelUnitCount(), a.getLabelSpan(), a.getFormat());
        return a;
    }

    public TimeAxisExt timeAxis (Element elem) throws Exception
    {
        return (null == elem) ? null : timeAxis(new TimeAxisExt(elem));
    }
    // any returned non-null object is added to the addDataSourcesAndArchives returned collection
    protected Object handleUnknownElement (Element elem, String tagName) throws Exception
    {
        // just so compiler does not complain about unreferenced parameters
        if ((null == elem) || (null == tagName) || (tagName.length() <= 0))
            throw new DOMException(DOMException.INVALID_STATE_ERR, "handleUnknownElement(" + tagName + ") incomplete arguments");

        throw new UnsupportedOperationException("handleUnknownElement(" + tagName + ")");
    }

    public Object addRenderingElement (final Element elem) throws Exception
    {
        if (null == elem)
            return null;

        final String    tagName=elem.getTagName();
        if (isDatasourceElementName(tagName))
            return datasource(elem);
        else if (LINE_ATTR.equalsIgnoreCase(tagName))
            return line(elem);
        else if (HRULE_ATTR.equalsIgnoreCase(tagName))
            return hrule(elem);
        else if (VRULE_ATTR.equalsIgnoreCase(tagName))
            return vrule(elem);
        else if (STACK_ATTR.equalsIgnoreCase(tagName))
            return stack(elem);
        else if (AREA_ATTR.equalsIgnoreCase(tagName))
            return area(elem);
        else if (TIME_AXIS_ATTR.equalsIgnoreCase(tagName))
            return timeAxis(elem);
        else
            return handleUnknownElement(elem, tagName);
    }
    // members can be datasource(s), line(s), stack(s), area(s), etc.
    public Collection<?> addRenderingElements (final Collection<? extends Element> nodes) throws Exception
    {
        final int    numNodes=(null == nodes) ? 0 : nodes.size();
        if (numNodes <= 0)
            return null;

        Collection<Object>    ret=null;
        for (final Element elem : nodes)
        {
            final Object    o=(null == elem) ? null : addRenderingElement(elem);
            if (null == o)
                continue;

            if (null == ret)
                ret = new LinkedList<Object>();
            ret.add(o);
        }

        return ret;
    }
    // members can be either a datasource or a line
    public Collection<?> addRenderingElements (Element root) throws Exception
    {
        return addRenderingElements(DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE));
    }

    public static final String    VALUE_AXIS_ATTR="valueAxis",
                                COLOR_ATTR="color";
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    public RrdGraphDefExt fromXml (Element elem) throws Exception
    {
        if (RrdGraphDefReflectiveProxy.DEFAULT.fromXml(this, elem) != this)
            throw new IllegalStateException("Mismatched recovered XML instances");

        return this;
    }

    public RrdGraphDefExt (Element elem) throws Exception
    {
        if (this != fromXml(elem))
            throw new IllegalStateException("Mismatched constructed instances");
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    public Element toXml (Document doc) throws Exception
    {
        throw new UnsupportedOperationException("toXml() N/A");
    }
}
