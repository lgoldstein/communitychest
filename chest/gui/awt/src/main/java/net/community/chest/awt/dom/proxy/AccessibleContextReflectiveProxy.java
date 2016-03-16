package net.community.chest.awt.dom.proxy;

import javax.accessibility.AccessibleContext;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.AttributeMethodType;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <C> The {@link AccessibleContext} type being reflected
 * @author Lyor G.
 * @since Mar 20, 2008 9:14:17 AM
 */
public class AccessibleContextReflectiveProxy<C extends AccessibleContext> extends UIReflectiveAttributesProxy<C> {
    protected AccessibleContextReflectiveProxy (Class<C> objClass, boolean registerAsDefault) throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public AccessibleContextReflectiveProxy (Class<C> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final String    ACESSIBLE_ATTR_PREFIX="accessible";
    /* Allow dropping the "Accessible" prefix from the setter name
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#getEffectiveAttributeName(java.lang.String)
     */
    @Override
    public String getEffectiveAttributeName (final String name)
    {
        final int    nLen=(null == name) ? 0 : name.length();
        if (nLen <= 0)
            return name;

        // check if starts with specified prefix already
        if (StringUtil.startsWith(name, ACESSIBLE_ATTR_PREFIX, true, false))
            return name;    // OK if already starts with prefix

        return ACESSIBLE_ATTR_PREFIX + AttributeMethodType.getAdjustedAttributeName(name);
    }

    public static final AccessibleContextReflectiveProxy<AccessibleContext>    ACCCTX=
        new AccessibleContextReflectiveProxy<AccessibleContext>(AccessibleContext.class, true);
}
