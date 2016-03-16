/*
 *
 */
package net.community.chest.beans;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Dec 1, 2011 10:52:12 AM
 */
public final class IntrospectionUtils {
    private IntrospectionUtils ()
    {
        // no instance
    }

    public static final Map<String,Object> introspect (final Object o)
            throws IntrospectionException, IllegalAccessException, InvocationTargetException
    {
        if (o == null)
            return null;

        final BeanInfo                info=Introspector.getBeanInfo(o.getClass());
        final PropertyDescriptor[]    props=info.getPropertyDescriptors();
        if ((props == null) || (props.length <= 0))
            return null;

        final Map<String,Object>    propsMap=new TreeMap<String,Object>();
        for (final PropertyDescriptor propDesc : props)
        {
            if (propDesc instanceof IndexedPropertyDescriptor)
                 continue;    // we are interested only in "pure" properties

            final Method    m=propDesc.getReadMethod();
            if (m == null)
                continue;    // OK just means the attribute is not readable

            final Object    value=m.invoke(o);
            if (value == null)
                continue;    // do not map null values

            final String    name=propDesc.getName();
            final Object    prev=propsMap.put(name, value);
            if (prev != null)
                throw new IllegalStateException("introspect(" + name + ")"
                                              + " multiple values (" + value + "/" + prev + ")"
                                              + " for object=" + o);
        }

        return propsMap;
    }

    public static final <O> O detrospect (final O o, final Map<String,?> propsMap)
            throws IntrospectionException, IllegalAccessException, InvocationTargetException
    {
        if ((o == null) || (propsMap == null) || propsMap.isEmpty())
            return o;

        final BeanInfo                info=Introspector.getBeanInfo(o.getClass());
        final PropertyDescriptor[]    props=info.getPropertyDescriptors();
        if ((props == null) || (props.length <= 0))
            return o;

        for (final PropertyDescriptor propDesc : props)
        {
            if (propDesc instanceof IndexedPropertyDescriptor)
                 continue;    // we are interested only in "pure" properties

            final Method    m=propDesc.getWriteMethod();
            if (m == null)
                continue;    // OK just means the attribute is not writeable

            final String    name=propDesc.getName();
            final Object    value=propsMap.get(name);
            if (value == null)
                continue;    // do not modify attributes for which there is no value

            m.invoke(o, value);
        }

        return o;
    }
}
