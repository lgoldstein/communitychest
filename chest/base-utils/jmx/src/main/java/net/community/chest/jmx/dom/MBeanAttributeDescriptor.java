package net.community.chest.jmx.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.management.MBeanAttributeInfo;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.lang.PubliclyCloneable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 2, 2008 12:57:38 PM
 */
public class MBeanAttributeDescriptor
        extends MBeanFeatureDescriptor<MBeanAttributeInfo>
        implements PubliclyCloneable<MBeanAttributeDescriptor> {
    /**
     *
     */
    private static final long serialVersionUID = 7805729761950372385L;
    public MBeanAttributeDescriptor ()
    {
        super(MBeanAttributeInfo.class);
    }

    public MBeanAttributeDescriptor (String name, String type, String description, Object value)
    {
        super(MBeanAttributeInfo.class, name, type, description, value);
    }

    public MBeanAttributeDescriptor (String name, String type, String description)
    {
        this(name, type, description, null);
    }

    public MBeanAttributeDescriptor (String name, String type, Object value)
    {
        this(name, type, null, value);
    }

    public MBeanAttributeDescriptor (String name, String type)
    {
        this(name, type, null);
    }

    public MBeanAttributeDescriptor (String name)
    {
        this(name, null);
    }

    public MBeanAttributeDescriptor (MBeanAttributeInfo aInfo)
    {
        this((null == aInfo) ? null : aInfo.getName(), (null == aInfo) ? null : aInfo.getType(), (null == aInfo) ? null : aInfo.getDescription());
        _accessType = MBeanAttributeAccessType.fromMBeanAttributeInfo(aInfo);
    }
    /*
     * @see net.community.chest.jmx.dom.MBeanFeatureDescriptor#clone()
     */
    @Override
    @CoVariantReturn
    public MBeanAttributeDescriptor clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    private Set<MBeanAttributeAccessType>    _accessType;
    public Set<MBeanAttributeAccessType> getAccessType ()
    {
        return _accessType;
    }

    public void setAccessType (Set<MBeanAttributeAccessType> accessType)
    {
        _accessType = accessType;
    }

    public static final String    ACCESS_ATTR="access";
    public Set<MBeanAttributeAccessType> setAccessType (Element elem)
    {
        final String    accType=(elem == null) ? null : elem.getAttribute(ACCESS_ATTR);
        _accessType = MBeanAttributeAccessType.fromAccessValue(accType);
        return _accessType;
    }

    public String addAccessType (final Element elem)
    {
        final String    accType=(elem == null) ? null : MBeanAttributeAccessType.toAccessValue(getAccessType());
        if ((accType == null) || (accType.length() <= 0))
            return accType;

        elem.setAttribute(ACCESS_ATTR, accType);
        return accType;
    }

    public static final String    ATTR_ELEM_NAME="attribute";
    /*
     * @see net.community.chest.jmx.dom.MBeanFeatureDescriptor#getMBeanFeatureElementName()
     */
    @Override
    public String getMBeanFeatureElementName ()
    {
        return ATTR_ELEM_NAME;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public MBeanAttributeDescriptor fromXml (Element elem) throws Exception
    {
        final Object    o=super.fromXml(elem);
        if (o != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");

        setAccessType(elem);
        return this;
    }

    public Element toXml (final Document doc, final boolean fetchValues, final boolean includeNulls) throws Exception
    {
        final Element    elem=super.toXml(doc);
        addAccessType(elem);

        if (fetchValues)
        {
            final String    vStr=resolveValueString();
            if (((vStr == null) || (vStr.length() <= 0)) && includeNulls)
                DOMUtils.addNonEmptyAttribute(elem, VALUE_ATTR, (null == vStr) ? NULL_PLACEHOLDER : EMPTY_PLACEHOLDER);
            else
                DOMUtils.addNonEmptyAttribute(elem, VALUE_ATTR, vStr);
        }

        return elem;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        return toXml(doc, true, true);
    }

    public MBeanAttributeDescriptor (Element elem) throws Exception
    {
        super(MBeanAttributeInfo.class);

        if (this != fromXml(elem))
            throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");
    }
    /**
     * Default {@link XmlValueInstantiator} for  {@link MBeanAttributeDescriptor}-s
     */
    public static final XmlValueInstantiator<MBeanAttributeDescriptor>    XMLINST=new XmlValueInstantiator<MBeanAttributeDescriptor>() {
            /*
             * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
             */
            @Override
            public MBeanAttributeDescriptor fromXml (Element elem) throws Exception
            {
                return (null == elem) ? null : new MBeanAttributeDescriptor(elem);
            }
        };

    public static final Map<String,Collection<MBeanAttributeDescriptor>> getMBeansAttributesDescriptors (final Map<String,Collection<MBeanAttributeInfo>>    mbaMap)
    {
        final Collection<? extends Map.Entry<String,? extends Collection<? extends MBeanAttributeInfo>>>    mbaSet=
            ((null == mbaMap) || (mbaMap.size() <= 0)) ? null : mbaMap.entrySet();
        if ((null == mbaSet) || (mbaSet.size() <= 0))
            return null;

        final Map<String,Collection<MBeanAttributeDescriptor>>    madMap=new TreeMap<String,Collection<MBeanAttributeDescriptor>>();
        for (final Map.Entry<String,? extends Collection<? extends MBeanAttributeInfo>> mae : mbaSet)
        {
            final String                                    mbName=(null == mae) ? null : mae.getKey();
            final Collection<? extends MBeanAttributeInfo>    mal=(null == mae) ? null : mae.getValue();
            final int                                        numAttrs=(null == mal) ? 0 : mal.size();
            if ((null == mbName) || (mbName.length() <= 0))
                continue;

            final Collection<MBeanAttributeDescriptor>    adl=
                (numAttrs <= 0) ? null : new ArrayList<MBeanAttributeDescriptor>(numAttrs);
            if (numAttrs > 0)
            {
                for (final MBeanAttributeInfo aInfo : mal)
                {
                    final MBeanAttributeDescriptor    ad=(null == aInfo) ? null : new MBeanAttributeDescriptor(aInfo);
                    if (ad != null)
                        adl.add(ad);
                }
            }

            if ((null == adl) || (adl.size() <= 0))
                continue;

            madMap.put(mbName, adl);
        }

        return madMap;
    }
}
