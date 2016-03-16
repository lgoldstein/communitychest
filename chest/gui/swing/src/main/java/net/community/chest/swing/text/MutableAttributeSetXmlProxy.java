/*
 *
 */
package net.community.chest.swing.text;

import java.lang.reflect.Method;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;
import net.community.chest.reflect.AttributeAccessor;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @param <V> Type of {@link MutableAttributeSet} being proxy-ed
 * @author Lyor G.
 * @since Jul 30, 2009 9:20:48 AM
 */
public class MutableAttributeSetXmlProxy<V extends MutableAttributeSet>
            extends UIReflectiveAttributesProxy<V> {

    protected MutableAttributeSetXmlProxy (Class<V> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public MutableAttributeSetXmlProxy (Class<V> vc)
    {
        this(vc, false);
    }
    /*
     * @see net.community.chest.dom.proxy.ReflectiveAttributesProxy#updateObjectResourceAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.Class, java.lang.reflect.Method)
     */
    @Override
    protected V updateObjectResourceAttribute (final V src, final String aName, final String aValue, final Class<?> t, final Method setter) throws Exception
    {
        final Object    o=loadObjectResourceAttribute(src, aName, aValue, t);
        setter.invoke(null, src, o);
        return src;
    }

    public V updateStyleAttribute (final V src, final String aName, final String aValue,
                                      final Class<?> aType, final Method setter) throws Exception
    {
        if ((aType != null) && Icon.class.isAssignableFrom(aType))
            return updateObjectResourceAttribute(src, aName, aValue, Icon.class, setter);

        final Object    objValue=getObjectAttributeValue(src, aName, aValue, aType);
        setter.invoke(null, src, objValue);
        return src;
    }

    public V updateStyleAttribute (final V src, final String aName, final String aValue, final Map<String,? extends Method> accsMap) throws Exception
    {
        final Map<String,? extends AttributeAccessor>    aMap=
            ((null == aName) || (aName.length() <= 0)) ? null : StyleConstantsUtils.getDefaultStyleConstantsSetters();
        final AttributeAccessor                            aa=
            ((null == aMap) || (aMap.size() <= 0)) ? null : aMap.get(aName);
        final Class<?>                                    aType=
            (null == aa) ? null : aa.getType();
        final Method                                    setter=
            (null == aa) ? null : aa.getSetter();
        if ((null == aType) || (null == setter))
            return super.handleUnknownAttribute(src, aName, aValue, accsMap);

        return updateStyleAttribute(src, aName, aValue, aType, setter);
    }
    /*
     * @see net.community.chest.dom.proxy.ReflectiveAttributesProxy#handleUnknownAttribute(java.lang.Object, java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    protected V handleUnknownAttribute (V src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
    {
        if (NAME_ATTR.equalsIgnoreCase(name))
            return src;

        return updateStyleAttribute(src, name, value, accsMap);
    }

    public static final MutableAttributeSetXmlProxy<SimpleAttributeSet> SIMPLESET=
        new MutableAttributeSetXmlProxy<SimpleAttributeSet>(SimpleAttributeSet.class, true);
}
