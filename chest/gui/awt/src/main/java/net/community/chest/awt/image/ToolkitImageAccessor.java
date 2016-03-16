/*
 *
 */
package net.community.chest.awt.image;

import java.awt.Image;
import java.lang.reflect.Method;
import java.util.Map;

import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 8, 2009 3:49:28 PM
 *
 */
public class ToolkitImageAccessor {
    public static final String    IMG_CLASS="sun.awt.image.ToolkitImage";
    private static Class<?>    _imgClass;
    public static final synchronized Class<?> getImageClass () throws ClassNotFoundException
    {
        if (null == _imgClass)
            _imgClass = ClassUtil.loadClassByName(IMG_CLASS);
        return _imgClass;
    }

    public static final boolean isImageClass (final Class<?> c)
    {
        if (null == c)
            return false;

        try
        {
            final Class<?>    ic=getImageClass();
            if ((ic != null) && ic.isAssignableFrom(c))
                return true;

            return false;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    public static final boolean isImageClass (final Object o)
    {
        if (o instanceof Image)
            return isImageClass(o.getClass());
        else
            return false;
    }

    private static Map<String,AttributeAccessor>    _accsMap;
    public static final synchronized Map<String,AttributeAccessor> getAccesors () throws ClassNotFoundException
    {
        if (null == _accsMap)
            _accsMap = AttributeMethodType.getAllAccessibleAttributes(getImageClass());
        return _accsMap;
    }

    private static <O> O invokeGetter (final Object o, final String name, final Class<O> oClass) throws Exception
    {
        if ((null == o) || (null == name) || (name.length() <= 0) || (null == oClass))
            return null;

        final Map<String,? extends AttributeAccessor>    am=getAccesors();
        final AttributeAccessor                            aa=
            (null == am) ? null : am.get(name);
        final Method                                    gm=
            (null == aa) ? null : aa.getGetter();
        final Object                                    gv=
            (null == gm) ? null : gm.invoke(o, AttributeAccessor.EMPTY_OBJECTS_ARRAY);
        if (null == gv)
            return null;

        return oClass.cast(gv);
    }

    private static Integer getInteger (final Object o, final String name) throws Exception
    {
        return invokeGetter(o, name, Integer.class);
    }

    public static final Integer getWidth (final Object o)
    {
        try
        {
            return getInteger(o, "width");
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public static final Integer getHeight (final Object o)
    {
        try
        {
            return getInteger(o, "height");
        }
        catch(Exception e)
        {
            return null;
        }
    }
}
