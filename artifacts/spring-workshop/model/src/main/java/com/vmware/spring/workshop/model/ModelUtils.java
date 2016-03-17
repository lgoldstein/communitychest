package com.vmware.spring.workshop.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author lgoldstein
 */
public final class ModelUtils {
    private ModelUtils() {
        // no instance
    }

    public static final Map<String,PropertyDescriptor> createPropertiesMap (final Class<?> clazz) throws IntrospectionException {
        final BeanInfo                            beanInfo=Introspector.getBeanInfo(clazz);
        final PropertyDescriptor[]                props=beanInfo.getPropertyDescriptors();
        final Map<String,PropertyDescriptor>    result=new TreeMap<String, PropertyDescriptor>(String.CASE_INSENSITIVE_ORDER);
        for (final PropertyDescriptor desc : props)
        {
            final String    name=desc.getName();
            final Method    gMethod=desc.getReadMethod(), sMethod=desc.getWriteMethod();
            if ((gMethod == null) || (sMethod == null))
                continue;    // skip non read/write properties

            final int    gMods=gMethod.getModifiers(), sMods=sMethod.getModifiers();
            if ((!Modifier.isPublic(gMods)) || (!Modifier.isPublic(sMods))
             || Modifier.isStatic(gMods) || Modifier.isStatic(sMods))
                 continue;    // skip static or non public methods

            final PropertyDescriptor    prev=result.put(name, desc);
            if (prev != null)
                throw new IntrospectionException("Multiple properties named " + name);
        }

        return result;
    }

}
