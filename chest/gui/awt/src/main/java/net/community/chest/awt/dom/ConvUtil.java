package net.community.chest.awt.dom;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.KeyStroke;
import javax.swing.border.Border;

import net.community.chest.awt.border.BorderValueInstantiator;
import net.community.chest.awt.dom.converter.ColorValueInstantiator;
import net.community.chest.awt.dom.converter.CursorValueInstantiator;
import net.community.chest.awt.dom.converter.DimensionValueInstantiator;
import net.community.chest.awt.dom.converter.InsetsValueInstantiator;
import net.community.chest.awt.dom.converter.KeyCodeValueInstantiator;
import net.community.chest.awt.dom.converter.KeyStrokeValueInstantiator;
import net.community.chest.awt.dom.converter.Line2DValueInstantiator;
import net.community.chest.awt.dom.converter.OrientationValueInstantiator;
import net.community.chest.awt.dom.converter.PointValueInstantiator;
import net.community.chest.awt.dom.converter.RectangleValueInstantiator;
import net.community.chest.awt.font.FontValueInstantiator;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.util.map.ClassNameMap;

/**
 * Copyright 2007 as per GPLv2
 *
 * Various static utilities
 *
 * @author Lyor G.
 * @since Jul 15, 2007 12:03:54 PM
 */
public final class ConvUtil {
    private ConvUtil ()
    {
        // no instance
    }

    public static final <M extends ClassNameMap<ValueStringInstantiator<?>>> M updateDefaultInstantiatorsMap (final M m)
    {
        if (null == m)
            return m;

        m.put(Color.class, ColorValueInstantiator.DEFAULT);
        m.put(Insets.class, InsetsValueInstantiator.DEFAULT);
        m.put(Rectangle.class, RectangleValueInstantiator.DEFAULT);
        m.put(Dimension.class, DimensionValueInstantiator.DEFAULT);
        m.put(Point2D.class, PointValueInstantiator.DEFAULT);
        m.put(Line2D.class, Line2DValueInstantiator.DEFAULT);
        m.put(KeyEvent.class, KeyCodeValueInstantiator.DEFAULT);
        m.put(KeyStroke.class, KeyStrokeValueInstantiator.DEFAULT);
        m.put(Font.class, FontValueInstantiator.DEFAULT);
        m.put(ComponentOrientation.class, OrientationValueInstantiator.DEFAULT);
        m.put(Cursor.class, CursorValueInstantiator.DEFAULT);

        // special generic instantiator(s)
        m.put(Border.class, BorderValueInstantiator.DEFAULT);
        return m;
    }

    public static final UIInstantiatorsMap createDefaultInstantiatorsMap ()
    {
        return updateDefaultInstantiatorsMap(new UIInstantiatorsMap());
    }

    private static Map<String,ValueStringInstantiator<?>>    _convsMap    /* =null */;
    // CAVEAT EMPTOR
    public static final synchronized Map<String,ValueStringInstantiator<?>> getConvertersMap ()
    {
        if (null == _convsMap)
            _convsMap = createDefaultInstantiatorsMap();
        return _convsMap;
    }
    // returns previous instance
    public static final synchronized Map<String,ValueStringInstantiator<?>> setConvertersMap (Map<String,ValueStringInstantiator<?>> m)
    {
        final Map<String,ValueStringInstantiator<?>>    prev=_convsMap;
        _convsMap = m;
        return prev;
    }

    @SuppressWarnings("unchecked")
    public static final <V> ValueStringInstantiator<V> getConverter (final Class<V> c)
    {
        if (null == c)
            return null;

        final Map<String,? extends ValueStringInstantiator<?>>    cMap=getConvertersMap();
        return (ValueStringInstantiator<V>) ClassNameMap.get(cMap, c);
    }
    /**
     * @param c {@link Class} that is candidate for XML conversion
     * @return TRUE if this attribute can be converted to/from XML
     */
    public static final boolean isConvertibleAttribute (final Class<?> c)
    {
        return (getConverter(c) != null);
    }
}
