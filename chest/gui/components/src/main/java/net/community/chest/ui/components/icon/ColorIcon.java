/*
 *
 */
package net.community.chest.ui.components.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.Icon;

import net.community.chest.CoVariantReturn;
import net.community.chest.awt.AWTUtils;
import net.community.chest.awt.attributes.Iconable;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 27, 2009 1:36:58 PM
 */
public class ColorIcon implements Icon, PubliclyCloneable<ColorIcon> {
    // NOTE !!! null is VALID margin
    protected static final boolean isValidMargin (final Insets margin)
    {
        if (null == margin)
            return true;

        // make sure insets leave some room
        if ((margin.top >= Iconable.DEFAULT_HEIGHT)
         || (margin.left >= Iconable.DEFAULT_WIDTH)
         || (margin.bottom >= Iconable.DEFAULT_HEIGHT)
         || (margin.right >= Iconable.DEFAULT_WIDTH))
            return false;

        final int    remWidth=Iconable.DEFAULT_WIDTH - (Math.max(0, margin.left) + Math.max(0, margin.right)),
                    remHeight=Iconable.DEFAULT_HEIGHT - (Math.max(margin.top,0) + Math.max(margin.bottom, 0));
        if ((remWidth <= 0) || (remHeight <= 0))
            return false;

        return true;
    }

    protected static final Rectangle getEnclosingRectangle (final Insets margin)
    {
        if (!isValidMargin(margin))
            return null;

        return AWTUtils.getDisplayArea(Iconable.DEFAULT_SIZE, margin);
    }

    protected static final boolean isValidRectangle (final Rectangle r)
    {
        if (null == r)
            return false;

        if ((r.x < 0) || (r.x >= Iconable.DEFAULT_WIDTH)
         || (r.y < 0) || (r.y >= Iconable.DEFAULT_HEIGHT)
         || (r.width <= 0) || (r.height <= 0))
            return false;

        return true;
    }
    /*
     * @see javax.swing.Icon#getIconHeight()
     */
    @Override
    public final int getIconHeight ()
    {
        return Iconable.DEFAULT_HEIGHT;
    }
    /*
     * @see javax.swing.Icon#getIconWidth()
     */
    @Override
    public final int getIconWidth ()
    {
        return Iconable.DEFAULT_WIDTH;
    }
    /**
     * Filled area {@link Color} - if <code>null</code> then only enclosing
     * line (if any) painted
     */
    private Color    _iconColor;
    public Color getIconColor ()
    {
        return _iconColor;
    }

    public void setIconColor (Color c)
    {
        _iconColor = c;
    }
    /**
     * The {@link IconShape} to use - if <code>null</code> then nothing
     * drawn
     */
    private IconShape    _iconShape;
    public IconShape getIconShape ()
    {
        return _iconShape;
    }

    public void setIconShape (IconShape s)
    {
        _iconShape = s;
    }

    private Insets    _margin;
    public Insets getMargin ()
    {
        return _margin;
    }

    public void setMargin (Insets margin)
    {
        _margin = margin;
    }
    /*
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
     */
    @Override
    public void paintIcon (Component c, Graphics g, int x, int y)
    {
        final IconShape    s=getIconShape();
        if (null == s)
            return;

        final Insets    margin=getMargin();
        g.translate(x, y);    // normalize the coordinates
        try
        {
            s.paintNormalizedShape(g, getEnclosingRectangle(margin), getIconColor());
        }
        finally
        {
            g.translate(-x, -y);    // restore coordinates
        }
    }

    public ColorIcon (IconShape iconShape, Color iconColor, Insets margin)
    {
        _iconShape = iconShape;
        _iconColor = iconColor;
        _margin = margin;
    }

    public ColorIcon (IconShape iconShape, Color iconColor)
    {
        this(iconShape, iconColor, null);
    }

    public static final Color    DEFAULT_ICON_COLOR=Color.WHITE;
    public ColorIcon (IconShape iconShape)
    {
        this(iconShape, DEFAULT_ICON_COLOR);
    }

    public ColorIcon ()
    {
        this(null);
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public ColorIcon clone ()
    {
        try
        {
            return getClass().cast(super.clone());
        }
        catch(CloneNotSupportedException e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (!(obj instanceof ColorIcon))
            return false;
        if (this == obj)
            return true;

        final ColorIcon    oi=(ColorIcon) obj;
        return (oi.getIconHeight() == getIconHeight())
            && (oi.getIconWidth() == getIconWidth())
            && AbstractComparator.compareObjects(oi.getIconShape(), getIconShape())
            && AbstractComparator.compareObjects(oi.getIconColor(), getIconColor())
            && AbstractComparator.compareObjects(oi.getMargin(), getMargin())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return getIconHeight()
             + getIconWidth()
             + ClassUtil.getObjectHashCode(getIconShape())
             + ClassUtil.getObjectHashCode(getIconColor())
             + ClassUtil.getObjectHashCode(getMargin())
        ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getIconShape() + "[" + getIconColor() + "]" + getMargin();
    }
}
