/*
 *
 */
package net.community.chest.jmx.dom;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.management.MBeanFeatureInfo;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.AbstractComparator;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <F> Type of {@link MBeanFeatureInfo} being described
 * @author Lyor G.
 * @since Mar 8, 2009 10:03:45 AM
 *
 */
public abstract class MBeanFeatureDescriptor<F extends MBeanFeatureInfo>
        extends BaseTypedValuesContainer<F>
        implements Serializable, Cloneable, XmlConvertible<MBeanFeatureDescriptor<F>> {
    /**
     *
     */
    private static final long serialVersionUID = 5841749910237244976L;
    protected MBeanFeatureDescriptor (Class<F> objClass)
            throws IllegalArgumentException
    {
        super(objClass);
    }

    private String    _name    /* =null */;
    public String getName ()
    {
        return _name;
    }

    public void setName (String name)
    {
        _name = name;
    }

    private String    _type    /* =null */;
    public String getType ()
    {
        return _type;
    }

    public void setType (String type)
    {
        _type = type;
    }
    // internal use depends on actual derived class
    private Object    _value    /* =null */;
    public Object getValue ()
    {
        return _value;
    }

    public void setValue (Object value)
    {
        _value = value;
    }

    private String _description;
    public String getDescription ()
    {
        return _description;
    }

    public void setDescription (String description)
    {
        _description = description;
    }

    public void clear ()
    {
        setName((String) null);
        setType((String) null);
        setDescription((String) null);
        setValue(null);
    }

    protected MBeanFeatureDescriptor (Class<F> objClass, String name, String type, String description, Object value)
    {
        this(objClass);

        _name = name;
        _type = type;
        _description = description;
        _value = value;
    }

    public static final String    NAME_ATTR="name",
                                TYPE_ATTR="type",
                                DESC_ATTR="description",
                                VALUE_ATTR="value";
    public String setName (Element elem)
    {
        final String    val=elem.getAttribute(NAME_ATTR);
        if ((val != null) && (val.length() > 0))
            setName(val);

        return val;
    }

    public String setType (Element elem)
    {
        final String    val=elem.getAttribute(TYPE_ATTR);
        if ((val != null) && (val.length() > 0))
            setType(val);

        return val;
    }

    public String setDescription (Element elem)
    {
        final String    val=elem.getAttribute(DESC_ATTR);
        if ((val != null) && (val.length() > 0))
            setDescription(val);

        return val;
    }

    public ValueStringInstantiator<?> getValueStringInstantiator (Object v)
    {
        final Class<?>    vClass;
        if (null == v)
        {
            final String    t=getType();
            if ((null == t) || (t.length() <= 0))
                return null;

            try
            {
                vClass = ClassUtil.resolveDataType(t);
            }
            catch(Exception e)
            {
                return null;
            }
        }
        else
            vClass = v.getClass();

        return (null == vClass) ? null : ClassUtil.getJDKStringInstantiator(vClass);
    }

    public ValueStringInstantiator<?> getValueStringInstantiator ()
    {
        return getValueStringInstantiator(getValue());
    }

    public String resolveStringList (final Collection<?> c)
    {
        if (c == null)
            return null;

        final StringBuilder    sb=new StringBuilder(2 + c.size() * 16).append("[");
        final int            sbLen=sb.length();
        for (final Object e : c)
        {
            final String    eStr=resolveValueString(e);
            if (sb.length() > sbLen)
                sb.append(',');
            sb.append(' ').append(eStr);
        }

        return sb.append(" ]").toString();
    }

    public String resolveStringMap (Map<?,?> m)
    {
        if (m == null)
            return null;

        final StringBuilder    sb=new StringBuilder(2 + m.size() * 32).append("[");
        final int            sbLen=sb.length();
        for (final Map.Entry<?,?> e : m.entrySet())
        {
            final Object    k=e.getKey(), v=e.getValue();
            final String    kStr=resolveValueString(k), vStr=resolveValueString(v);
            if (sb.length() > sbLen)
                sb.append(',');
            sb.append(' ')
              .append(kStr)
              .append('=')
              .append(vStr)
              ;
        }

        return sb.append(" ]").toString();
    }

    public String resolveStringArray (Object o, Class<?> aType)
    {
        final Class<?>    tEquiv=ClassUtil.getPrimitiveTypeEquivalent(aType);
        if (tEquiv == null)
            return resolveStringList(Arrays.asList((Object[]) o));

        // for primitive types we turn them into their wrapper classes
        final int        aLen=Array.getLength(o);
        final Object[]    aVals=new Object[aLen];
        for (int    aIndex=0; aIndex < aLen; aIndex++)
            aVals[aIndex] = Array.get(o, aIndex);

        return resolveStringList(Arrays.asList(aVals));
    }

    public String resolveValueString (Object o)
    {
        final Class<?>    c=(o == null) ? null : o.getClass();
        if (c == null)
            return null;

        if (c.isArray())
            return resolveStringArray(o, c.getComponentType());
        else if (Collection.class.isAssignableFrom(c))
            return resolveStringList((Collection<?>) o);
        else if (Map.class.isAssignableFrom(c))
            return resolveStringMap((Map<?,?>) o);

        final ValueStringInstantiator<?>    vsi=getValueStringInstantiator(o);
        if (null == vsi)
            return o.toString();

        try
        {
            @SuppressWarnings("unchecked")
            final ValueStringInstantiator<? super Object>    vso=
                    (ValueStringInstantiator<? super Object>) vsi;
            return vso.convertInstance(o);
        }
        catch(Exception e)
        {
            return o.toString();
        }
    }
    /**
     * @return The value as a string by using a {@link ValueStringInstantiator}
     * if any available
     */
    public String resolveValueString ()
    {
        return resolveValueString(getValue());
    }

    public static final String    NULL_PLACEHOLDER="null", EMPTY_PLACEHOLDER="-empty-";
    public Object resolveValueFromString (final String org) throws Exception
    {
        final String    val;
        if (NULL_PLACEHOLDER.equals(org))
            val = null;
        else if (EMPTY_PLACEHOLDER.equals(org))
            val = "";
        else
            val = org;
        if ((null == val) || (val.length() <= 0))
            return null;

        final ValueStringInstantiator<?>    vsi=getValueStringInstantiator();
        // if no instantiator assume the string itself
        final Object                        v=(null == vsi) ? val : vsi.newInstance(val);
        setValue(v);
        return v;
    }

    public Object resolveValue (Element elem) throws Exception
    {
        return resolveValueFromString(elem.getAttribute(VALUE_ATTR));
    }

    public abstract String getMBeanFeatureElementName ();
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public MBeanFeatureDescriptor<F> fromXml (Element elem) throws Exception
    {
        if (null == elem)
            throw new DOMException(DOMException.VALIDATION_ERR, "fromXml() no XML element");

        setName(elem);
        setType(elem);
        setDescription(elem);
        resolveValue(elem);
        return this;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    elem=doc.createElement(getMBeanFeatureElementName());
        DOMUtils.addNonEmptyAttribute(elem, NAME_ATTR, getName());
        DOMUtils.addNonEmptyAttribute(elem, TYPE_ATTR, getType());
        DOMUtils.addNonEmptyAttribute(elem, DESC_ATTR, getDescription());
        return elem;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final String    n=getName(), t=getType(), d=getDescription(),
                        v=resolveValueString(), en=getMBeanFeatureElementName();
        final int        nLen=(null == n) ? 0 : n.length(),
                        tLen=(null == t) ? 0 : t.length(),
                        vLen=(null == v) ? 0 : v.length(),
                        dLen=(null == d) ? 0 : d.length(),
                        enLen=(null == en) ? 0 : en.length(),
                        totalLen=Math.max(enLen, 0)
                            + Math.max(nLen,0)
                            + Math.max(tLen,0)
                            + Math.max(vLen,0)
                            + Math.max(dLen,0)
                            + 32
                            ;
        final StringBuilder    sb=new StringBuilder(totalLen).append('<').append(en);
        if (nLen > 0)
            sb.append(' ')
              .append(NAME_ATTR)
              .append("=\"")
              .append(n)
              .append('"')
              ;
        if (tLen > 0)
            sb.append(' ')
              .append(TYPE_ATTR)
              .append("=\"")
              .append(t)
              .append('"')
              ;
        if (vLen > 0)
            sb.append(' ')
              .append(VALUE_ATTR)
              .append("=\"")
              .append(v)
              .append('"')
              ;
        if (dLen > 0)
            sb.append(' ')
              .append(DESC_ATTR)
              .append("=\"")
              .append(d)
              .append('"')
              ;
        sb.append("/>");

        return sb.toString();
    }

    public boolean isSameDescriptor (final MBeanFeatureDescriptor<?> ad)
    {
        if (ad == null)
            return false;
        if (ad == this)
            return true;

        return (0 == StringUtil.compareDataStrings(getName(), ad.getName(), false))
            && (0 == StringUtil.compareDataStrings(getType(), ad.getType(), true))
            && AbstractComparator.compareObjects(getValue(), ad.getValue())
            ;    // NOTE: we do not compare description(s)
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        final Class<?>    oc=(obj == null) ? null : obj.getClass();
        if (oc != getClass())
            return false;

        return isSameDescriptor((MBeanFeatureDescriptor<?>) obj);
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return StringUtil.getDataStringHashCode(getName(), false)
             + StringUtil.getDataStringHashCode(getType(), true)
             + ClassUtil.getObjectHashCode(getValue())
             ;
    }
    /*
     * @see java.lang.Object#clone()
     */
    @SuppressWarnings("unchecked")
    @Override
    public MBeanFeatureDescriptor<F> clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}
