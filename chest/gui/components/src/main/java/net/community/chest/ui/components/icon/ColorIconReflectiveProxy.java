/*
 *
 */
package net.community.chest.ui.components.icon;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <I> The reflected {@link ColorIcon} class</I>
 * @author Lyor G.
 * @since Jan 27, 2009 2:13:49 PM
 */
public class ColorIconReflectiveProxy<I extends ColorIcon> extends UIReflectiveAttributesProxy<I> {
    protected ColorIconReflectiveProxy (Class<I> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public ColorIconReflectiveProxy (Class<I> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final String    ICON_SHAPE_ATTR=IconShape.class.getSimpleName();
    /*
     * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected I updateObjectAttribute (I src, String name, String value, Method setter) throws Exception
    {
        if (ICON_SHAPE_ATTR.equalsIgnoreCase(name))
        {
            final IconShape    s=IconShape.fromString(value);
            if (null == s)
                throw new NoSuchElementException("updateObjectAttribute(" + name + ")[" + value + "] unknown value");
            src.setIconShape(s);
            return src;
        }

        return super.updateObjectAttribute(src, name, value, setter);
    }

    public static final ColorIconReflectiveProxy<ColorIcon>    CLRICN=
        new ColorIconReflectiveProxy<ColorIcon>(ColorIcon.class, true);
}
