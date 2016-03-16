/*
 *
 */
package net.community.chest.reflect.beans;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.reflect.AttributeAccessor;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 14, 2010 10:00:04 AM
 */
public final class IntrospectorUtils {
    private IntrospectorUtils ()
    {
        // no instance
    }

    public static final Map<String,AttributeAccessor> toAccessorsMap (final BeanInfo bi)
    {
        final PropertyDescriptor[]    props=(bi == null) ? null : bi.getPropertyDescriptors();
        if ((props == null) || (props.length <= 0))
            return null;

        Map<String,AttributeAccessor>    accsMap=null;
        for (final PropertyDescriptor p : props)
        {
            final AttributeAccessor    a=toAttributeAccessor(p);
            if (a == null)
                continue;

            if (accsMap == null)
                accsMap = new TreeMap<String,AttributeAccessor>(String.CASE_INSENSITIVE_ORDER);

            final AttributeAccessor    prev=accsMap.put(a.getName(), a);
            if (prev != null)    // TODO consider throwing an exception
                continue;
        }

        return accsMap;
    }

    public static final AttributeAccessor toAttributeAccessor (final PropertyDescriptor p)
    {
        if (p == null)
            return null;

        final AttributeAccessor    a=new AttributeAccessor();
        a.setName(p.getName());
        a.setGetter(p.getReadMethod());
        a.setSetter(p.getWriteMethod());
        a.setType(p.getPropertyType());
        return a;
    }
}
