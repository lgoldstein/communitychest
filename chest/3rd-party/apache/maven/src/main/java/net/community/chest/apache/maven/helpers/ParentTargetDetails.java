package net.community.chest.apache.maven.helpers;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 13, 2007 1:42:27 PM
 */
public class ParentTargetDetails extends BaseTargetDetails {
    /**
     *
     */
    private static final long serialVersionUID = -2931531284822413859L;
    public ParentTargetDetails ()
    {
        super();
    }

    private String    _relativePath    /* =null */;
    /**
     * @return the &lt;relativePath&gt; element value
     */
    public String getRelativePath ()
    {
        return _relativePath;
    }

    public void setRelativePath (String relativePath)
    {
        _relativePath = relativePath;
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#clear()
     */
    @Override
    public void clear ()
    {
        super.clear();
        setRelativePath((String) null);
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#clone()
     */
    @Override
    @CoVariantReturn
    public ParentTargetDetails clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#equals(java.lang.Object)
     */
    @Override
    public boolean equals (final Object obj)
    {
        if (!super.equals(obj))
            return false;
        if (this == obj)
            return true;
        if (!(obj instanceof ParentTargetDetails))
            return false;

        final ParentTargetDetails    td=(ParentTargetDetails) obj;
        return (0 == StringUtil.compareDataStrings(getRelativePath(), td.getRelativePath(), true))
            ;
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return super.hashCode()
             + StringUtil.getDataStringHashCode(getRelativePath(), false)
             ;
    }

    public static final String    RELPATH_ELEM_NAME="relativePath";
    public String setRelativePath (final Element elem) throws Exception
    {
        final String    val=DOMUtils.getElementStringValue(elem);
        if ((val != null) && (val.length() > 0))
            setRelativePath(val);

        return val;
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#handleUnknownElement(org.w3c.dom.Element, java.lang.String)
     */
    @Override
    public void handleUnknownElement (Element elem, String tagName) throws Exception
    {
        if (RELPATH_ELEM_NAME.equalsIgnoreCase(tagName))
            setRelativePath(elem);
        else
            super.handleUnknownElement(elem, tagName);
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#fromXml(org.w3c.dom.Element)
     */
    @Override
    public ParentTargetDetails fromXml (Element root) throws Exception
    {
        return getClass().cast(super.fromXml(root));
    }

    public ParentTargetDetails (final Element elem) throws Exception
    {
        final ParentTargetDetails    inst=fromXml(elem);
        if (inst != this)
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched instances");
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        // TODO implement toXml
        throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
    }
}
