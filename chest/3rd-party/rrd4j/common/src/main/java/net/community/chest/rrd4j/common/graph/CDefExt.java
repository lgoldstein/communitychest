package net.community.chest.rrd4j.common.graph;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 15, 2008 10:32:49 AM
 */
public class CDefExt extends SourceExt /* TODO extends CDef */ {
    private String    _rpnExpression    /* =null */;
    public String getRpnExpression ()
    {
        return _rpnExpression;
    }

    public void setRpnExpression (String rpnExpression)
    {
        _rpnExpression = rpnExpression;
    }

    public static final String RPN_EXPR_ATTR="rpn";
    public String setRpnExpression (Element elem)
    {
        final String    val=elem.getAttribute(RPN_EXPR_ATTR);
        if ((val != null) && (val.length() > 0))
            setRpnExpression(val);

        return val;
    }

    public Element addRpnExpression (Element elem)
    {
        return DOMUtils.addNonEmptyAttribute(elem, RPN_EXPR_ATTR, getRpnExpression());
    }

    public CDefExt (String name, String rpnExpression)
    {
        super(name);
        _rpnExpression = rpnExpression;
    }

    public CDefExt (String name)
    {
        this(name, null);
    }

    public CDefExt ()
    {
        super();
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#clone()
     */
    @Override
    @CoVariantReturn
    public CDefExt clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#getRootElementName()
     */
    @Override
    public String getRootElementName ()
    {
        return RrdGraphDefExt.CDEF_ELEM_NAME;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public CDefExt fromXml (Element elem) throws Exception
    {
        if (this != super.fromXml(elem))
            throw new IllegalStateException("Mismatched recovered XML data instances");

        setRpnExpression(elem);
        return this;
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#toString()
     */
    @Override
    public String toString ()
    {
        return super.toString() + ":RPN=" + getRpnExpression();
    }
    /*
     * @see net.community.chest.rrd4j.common.graph.SourceExt#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    elem=super.toXml(doc);
        addRpnExpression(elem);
        return elem;
    }

    public CDefExt (Element elem) throws Exception
    {
        super(elem);
    }
}
