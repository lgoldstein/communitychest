/*
 *
 */
package net.community.chest.swing.text;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;

import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 30, 2009 9:42:47 AM
 */
public final class StyleConstantsUtils {
    private StyleConstantsUtils ()
    {
        // no instance
    }

    public static final Map<String,AttributeAccessor> getStyleConstantsSetters (final boolean errIfDuplicate)
    {
        final Method[]    ma=StyleConstants.class.getMethods();
        if ((null == ma) || (ma.length <= 0))
            return null;

        Map<String,AttributeAccessor>    ret=null;
        for (final Method m : ma)
        {
            final String    n=(null == m) ? null : m.getName();
            if (!AttributeMethodType.SETTER.isMatchingPrefix(n))
                continue;
            if (!AttributeMethodType.SETTER.isMatchingReturnType(m))
                continue;

            final Class<?>[]    pa=m.getParameterTypes();
            if ((null == pa) || (pa.length != 2))
                continue;

            final Class<?>    p1=pa[0];
            if ((null == p1) || (!p1.isAssignableFrom(MutableAttributeSet.class)))
                continue;

            final Class<?>            aType=pa[1];
            final String            aName=
                AttributeMethodType.SETTER.getPureAttributeName(n);
            final AttributeAccessor    aa=new AttributeAccessor(aName);
            aa.setType(aType);
            aa.setSetter(m);

            if (null == ret)
                ret = new TreeMap<String,AttributeAccessor>(String.CASE_INSENSITIVE_ORDER);

            final AttributeAccessor    prev=ret.put(aName, aa);
            if ((prev != null) && errIfDuplicate)
                throw new IllegalStateException("getStyleConstantsSetters(" + aName + ") duplicate methods");
        }

        return ret;
    }

    private static Map<String,AttributeAccessor>    _styleSettersMap;
    public static final synchronized Map<String,AttributeAccessor> getDefaultStyleConstantsSetters ()
    {
        if (null == _styleSettersMap)
            _styleSettersMap = getStyleConstantsSetters(true);
        return _styleSettersMap;
    }

    public static final Icon getIconAttribute (final AttributeSet s)
    {
        if ((null == s) || (!s.isDefined(StyleConstants.IconAttribute)))
            return null;

        return (Icon) s.getAttribute(StyleConstants.IconAttribute);
    }

    public static final Icon removeIconAttribute (final MutableAttributeSet s)
    {
        final Icon    i=getIconAttribute(s);
        if (i != null)
        {
            s.removeAttribute(StyleConstants.IconAttribute);

            if (s.isDefined(AbstractDocument.ElementNameAttribute))
                s.removeAttribute(AbstractDocument.ElementNameAttribute);
        }

        return i;
    }
}
