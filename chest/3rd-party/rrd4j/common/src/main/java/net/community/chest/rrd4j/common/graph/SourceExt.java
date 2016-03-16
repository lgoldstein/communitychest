package net.community.chest.rrd4j.common.graph;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.rrd4j.common.RrdUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 15, 2008 10:21:43 AM
 */
public abstract class SourceExt /* TODO extends Source */
        implements PubliclyCloneable<SourceExt>, XmlConvertible<SourceExt> {
    private String    _name    /* =null */;
    public String getName ()
    {
        return _name;
    }

    public void setName (String name)
    {
        _name = name;
    }

    public static final String    NAME_ATTR="name";
    public Element addName (Element elem)
    {
        return DOMUtils.addNonEmptyAttribute(elem, NAME_ATTR, getName());
    }

    public String setName (Element elem)
    {
        final String    val=elem.getAttribute(NAME_ATTR);
        if ((val != null) && (val.length() > 0))
            setName(val);

        return val;
    }

    protected SourceExt (String name)
    {
        _name = name;
    }

    protected SourceExt ()
    {
        super();
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    public SourceExt fromXml (Element elem) throws Exception
    {
        setName(elem);
        return this;
    }

    public SourceExt (Element elem) throws Exception
    {
        if (this != fromXml(elem))
            throw new IllegalStateException("Mismatched re-constructed instances");
    }

    public static final String getRootElementName (final Class<?> c)
    {
        if (null == c)
            return null;

        final String    clsName=c.getSimpleName();
        if (clsName.endsWith(RrdUtils.DEFAULT_EXTENSION_CLASS_SUFFIX))
            return clsName.substring(0, clsName.length() - RrdUtils.DEFAULT_EXTENSION_CLASS_SUFFIX.length());

        return clsName;
    }

    public abstract String getRootElementName ();
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    public Element toXml (Document doc) throws Exception
    {
        final Element    elem=doc.createElement(getRootElementName());
        addName(elem);
        return elem;
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public SourceExt clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getRootElementName() + "[" + getName() + "]";
    }
}
