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
 * <P>Represents a Maven build target details</P
 *
 * @author Lyor G.
 * @since Aug 8, 2007 3:46:18 PM
 */
public class BuildTargetDetails extends BaseTargetDetails {
    /**
     *
     */
    private static final long serialVersionUID = 8181742508671674437L;
    public BuildTargetDetails ()
    {
        super();
    }

    private String    _modelVersion;
    /**
     * @return the &lt;modelVersion&gt; value
     */
    public String getModelVersion ()
    {
        return _modelVersion;
    }

    public void setModelVersion (String modelVersion)
    {
        _modelVersion = modelVersion;
    }

    private String    _name;
    /**
     * @return the &lt;name&gt; value
     */
    public String getName ()
    {
        return _name;
    }

    public void setName (String name)
    {
        _name = name;
    }

    private String    _packaging;
    /**
     * @return the &lt;packaging&gt; value - if missing then default for
     * project type is assumed
     */
    public String getPackaging ()
    {
        return _packaging;
    }

    public void setPackaging (String packaging)
    {
        _packaging = packaging;
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#clear()
     */
    @Override
    public void clear ()
    {
        setModelVersion((String) null);
        setName((String) null);
        setPackaging((String) null);

        super.clear();
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public BuildTargetDetails clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#equals(java.lang.Object)
     */
    @Override
    public boolean equals (final Object obj)
    {
        if (!(obj instanceof BuildTargetDetails))
            return false;
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;

        final BuildTargetDetails    td=(BuildTargetDetails) obj;
        return (0 == StringUtil.compareDataStrings(getModelVersion(), td.getModelVersion(), true))
            && (0 == StringUtil.compareDataStrings(getName(), td.getName(), true))
            && (0 == StringUtil.compareDataStrings(getPackaging(), td.getPackaging(), false))
            ;
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return super.hashCode()
             + StringUtil.getDataStringHashCode(getModelVersion(), true)
             + StringUtil.getDataStringHashCode(getName(), true)
             + StringUtil.getDataStringHashCode(getPackaging(), false)
        ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return super.toString() + "[" + getName() + "]";
    }

    public static final String    MODELVERSION_ELEM_NAME="modelVersion",
                                    DEFAULT_MODEL_VERSION="4.0.0";
    public String setModelVersion (final Element elem) throws Exception
    {
        final String    val=DOMUtils.getElementStringValue(elem);
        if ((val != null) && (val.length() > 0))
            setModelVersion(val);

        return val;
    }

    public static final String    NAME_ELEM_NAME="name";
    public String setName (final Element elem) throws Exception
    {
        final String    val=DOMUtils.getElementStringValue(elem);
        if ((val != null) && (val.length() > 0))
            setName(val);

        return val;
    }

    public static final String    PACKAGING_ELEM_NAME="packaging";
    public String setPackaging (final Element elem) throws Exception
    {
        final String    val=DOMUtils.getElementStringValue(elem);
        if ((val != null) && (val.length() > 0))
            setPackaging(val);

        return val;
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#handleUnknownElement(org.w3c.dom.Element, java.lang.String)
     */
    @Override
    public void handleUnknownElement (final Element elem, final String tagName) throws Exception
    {
        if (MODELVERSION_ELEM_NAME.equalsIgnoreCase(tagName))
            setModelVersion(elem);
        else if (NAME_ELEM_NAME.equalsIgnoreCase(tagName))
            setName(elem);
        else if (PACKAGING_ELEM_NAME.equalsIgnoreCase(tagName))
            setPackaging(elem);
        else
            super.handleUnknownElement(elem, tagName);
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public BuildTargetDetails fromXml (Element root) throws Exception
    {
        return getClass().cast(super.fromXml(root));
    }

    public BuildTargetDetails (final Element elem) throws Exception
    {
        final BuildTargetDetails    inst=fromXml(elem);
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
