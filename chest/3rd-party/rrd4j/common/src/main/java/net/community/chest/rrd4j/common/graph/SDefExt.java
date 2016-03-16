package net.community.chest.rrd4j.common.graph;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.rrd4j.common.ConsolFunExt;

import org.rrd4j.ConsolFun;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 15, 2008 10:49:33 AM
 */
public class SDefExt extends SourceExt /* TODO extends SDef */ {
    public SDefExt ()
    {
        super();
    }

    private String _defName;
    public String getDefName ()
    {
        return _defName;
    }

    public void setDefName (String defName)
    {
        _defName = defName;
    }

    public static final String    DEF_NAME_ATTR="defName";
    public String setDefName (Element elem)
    {
        final String    val=elem.getAttribute(DEF_NAME_ATTR);
        if ((val != null) && (val.length() > 0))
            setDefName(val);

        return val;
    }

    public Element addDefName (Element elem)
    {
        return DOMUtils.addNonEmptyAttribute(elem, DEF_NAME_ATTR, getDefName());
    }

    private ConsolFun _consolFun;
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
        final ConsolFun    val=ConsolFunExt.DEFAULT.fromXml(elem);
        if (val != null)
            setConsolFun(val);

        return val;
    }

    public Element addConsolFun (Element elem) throws Exception
    {
        ConsolFunExt.DEFAULT.toXml(elem, getConsolFun());
        return elem;
    }

    public SDefExt (String name, String defName, ConsolFun consolFun)
    {
        super(name);

        _defName = defName;
        _consolFun = consolFun;
    }

    public SDefExt (String name, String defName)
    {
        this(name, defName, null);
    }

    public SDefExt (String name)
    {
        this(name, null);
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#getRootElementName()
     */
    @Override
    public String getRootElementName ()
    {
        return RrdGraphDefExt.SDEF_ELEM_NAME;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#clone()
     */
    @Override
    @CoVariantReturn
    public SDefExt clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public SDefExt fromXml (Element elem) throws Exception
    {
        if (this != super.fromXml(elem))
            throw new IllegalStateException("Mismatched recovered XML instances");

        setDefName(elem);
        setConsolFun(elem);

        return this;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#toString()
     */
    @Override
    public String toString ()
    {
        return super.toString() + "{DEF=" + getDefName() + "}:" + getConsolFun();
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    elem=super.toXml(doc);
        addDefName(elem);
        addConsolFun(elem);
        return elem;
    }

    public SDefExt (Element elem) throws Exception
    {
        super(elem);
    }
}
