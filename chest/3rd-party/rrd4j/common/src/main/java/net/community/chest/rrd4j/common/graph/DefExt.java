package net.community.chest.rrd4j.common.graph;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.resources.SystemPropertiesResolver;
import net.community.chest.rrd4j.common.ConsolFunExt;

import org.rrd4j.ConsolFun;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 21, 2008 1:43:34 PM
 */
public class DefExt extends SourceExt /* TODO extends Def */ {
    private String _rrdPath    /* =null */, _dsName /* =null */, _backend /* =null */;
    public String getDsName ()
    {
        return _dsName;
    }

    public void setDsName (String dsName)
    {
        _dsName = dsName;
    }

    public static final String    DSNAME_ATTR="dsName";
    public Element addDsName (Element elem)
    {
        return DOMUtils.addNonEmptyAttribute(elem, DSNAME_ATTR, getDsName());
    }

    public String setDsName (Element elem)
    {
        final String    val=elem.getAttribute(DSNAME_ATTR);
        if ((val != null) && (val.length() > 0))
            setDsName(val);

        return val;
    }

    public String getBackend ()
    {
        return _backend;
    }

    public void setBackend (String backend)
    {
        _backend = backend;
    }

    public static final String    BACKEND_ATTR="backend";
    public Element addBackend (Element elem)
    {
        return DOMUtils.addNonEmptyAttribute(elem, BACKEND_ATTR, getDsName());
    }

    public String setBackend (Element elem)
    {
        final String    val=elem.getAttribute(BACKEND_ATTR);
        if ((val != null) && (val.length() > 0))
            setBackend(val);

        return val;
    }

    public String getRrdPath ()
    {
        return _rrdPath;
    }

    public void setRrdPath (String rrdPath)
    {
        _rrdPath = rrdPath;
    }

    public static final String    RRDPATH_ATTR="rrdPath";
    public Element addRrdPath (Element elem)
    {
        return DOMUtils.addNonEmptyAttribute(elem, RRDPATH_ATTR, getDsName());
    }

    public String setRrdPath (Element elem)
    {
        final String    val=elem.getAttribute(RRDPATH_ATTR);
        final String    v=((null == val) || (val.length() <= 0)) ? null : SystemPropertiesResolver.SYSTEM.format(val);
        if ((v != null) && (v.length() > 0))
            setRrdPath(v);

        return v;
    }

    private ConsolFun _consolFun /* =null */;
    public ConsolFun getConsolFun ()
    {
        return _consolFun;
    }

    public void setConsolFun (ConsolFun consolFun)
    {
        _consolFun = consolFun;
    }

    public ConsolFun setConsolFun (Element elem) throws Exception
    {
        final ConsolFun    f=ConsolFunExt.DEFAULT.fromXml(elem);
        if (f != null)
            setConsolFun(f);

        return f;
    }

    public Element addConsolFun (Element elem) throws Exception
    {
        ConsolFunExt.DEFAULT.toXml(elem, getConsolFun());
        return elem;
    }

    public DefExt (String name, String rrdPath, String dsName, ConsolFun consolFun, String backend)
    {
        super(name);

        _rrdPath = rrdPath;
        _dsName = dsName;
        _consolFun = consolFun;
        _backend = backend;
    }

    public DefExt (String name, String rrdPath, String dsName, ConsolFun consolFun)
    {
        this(name, rrdPath, dsName, consolFun, null);
    }

    public DefExt (String name)
    {
        super(name);
    }

    public DefExt ()
    {
        super();
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#getRootElementName()
     */
    @Override
    public String getRootElementName ()
    {
        return RrdGraphDefExt.DEF_ELEM_NAME;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#clone()
     */
    @Override
    @CoVariantReturn
    public DefExt clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public DefExt fromXml (Element elem) throws Exception
    {
        if (super.fromXml(elem) != this)
            throw new IllegalStateException("Mismatched recovered XML instance");

        setRrdPath(elem);
        setDsName(elem);
        setConsolFun(elem);
        setBackend(elem);

        return this;
    }

    public DefExt (Element elem) throws Exception
    {
        super(elem);
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#toString()
     */
    @Override
    public String toString ()
    {
        return super.toString()
            + "(RRDPath=" + getRrdPath() + ")"
            + "(DSNAME=" + getDsName() + ")"
            + "(CONSOLE=" + getConsolFun() + ")"
            + "(BACKEND=" + getBackend() + ")"
            ;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    elem=super.toXml(doc);
        addRrdPath(elem);
        addDsName(elem);
        addConsolFun(elem);
        addBackend(elem);
        return elem;
    }
}
