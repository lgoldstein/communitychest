/*
 *
 */
package net.community.chest.jmx.dom;

import javax.management.MBeanParameterInfo;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.PubliclyCloneable;

import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 8, 2009 9:59:31 AM
 *
 */
public class MBeanParameterDescriptor
            extends MBeanFeatureDescriptor<MBeanParameterInfo>
            implements PubliclyCloneable<MBeanParameterDescriptor> {
    /**
     *
     */
    private static final long serialVersionUID = 5405035848545237724L;
    /**
     * Default constructor
     */
    public MBeanParameterDescriptor ()
    {
        super(MBeanParameterInfo.class);
    }
    public MBeanParameterDescriptor (String name, String type, String description, Object value)
    {
        super(MBeanParameterInfo.class, name, type, description, value);
    }

    public MBeanParameterDescriptor (String name, String type, String description)
    {
        this(name, type, description, null);
    }

    public MBeanParameterDescriptor (String name, String type, Object value)
    {
        this(name, type, null, value);
    }

    public MBeanParameterDescriptor (String name, String type)
    {
        this(name, type, null);
    }

    public MBeanParameterDescriptor (String name)
    {
        this(name, null);
    }

    public MBeanParameterDescriptor (MBeanParameterInfo aInfo)
    {
        this((null == aInfo) ? null : aInfo.getName(), (null == aInfo) ? null : aInfo.getType(), (null == aInfo) ? null : aInfo.getDescription());
    }

    public static final String    PARAM_ELEM_NAME="param";
    /*
     * @see net.community.chest.jmx.dom.MBeanFeatureDescriptor#getMBeanFeatureElementName()
     */
    @Override
    public String getMBeanFeatureElementName ()
    {
        return PARAM_ELEM_NAME;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public MBeanParameterDescriptor fromXml (Element elem) throws Exception
    {
        final Object    o=super.fromXml(elem);
        if (o != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");
        return this;
    }

    public MBeanParameterDescriptor (Element elem) throws Exception
    {
        super(MBeanParameterInfo.class);

        if (this != fromXml(elem))
            throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");
    }
    /*
     * @see net.community.chest.jmx.dom.MBeanFeatureDescriptor#clone()
     */
    @Override
    @CoVariantReturn
    public MBeanParameterDescriptor clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}
