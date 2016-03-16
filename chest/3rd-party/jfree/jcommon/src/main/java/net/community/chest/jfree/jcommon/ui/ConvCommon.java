/*
 *
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.Map;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.jfree.jcommon.util.RotationValueStringInstantiator;
import net.community.chest.jfree.jcommon.util.SortOrderEnumStringInstantiator;
import net.community.chest.jfree.jcommon.util.TableOrderEnumStringInstantiator;
import net.community.chest.util.map.ClassNameMap;

import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.ui.VerticalAlignment;
import org.jfree.util.Rotation;
import org.jfree.util.SortOrder;
import org.jfree.util.TableOrder;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 27, 2009 4:32:16 PM
 */
public final class ConvCommon {
    private ConvCommon ()
    {
        // no instance
    }

    public static final <M extends ClassNameMap<ValueStringInstantiator<?>>> M updateDefaultInstantiatorsMap (final M m)
    {
        if (null == m)
            return m;

        m.put(RectangleInsets.class, RectangleInsetsValueStringInstantiator.DEFAULT);
        m.put(HorizontalAlignment.class, HAlignmentValueStringInstantiator.DEFAULT);
        m.put(VerticalAlignment.class, VAlignmentValueStringInstantiator.DEFAULT);
        m.put(RectangleEdge.class, RectEdgeValueStringInstantiator.DEFAULT);
        m.put(RectangleAnchor.class, RectAnchorValueStringInstantiator.DEFAULT);
        m.put(Rotation.class, RotationValueStringInstantiator.DEFAULT);
        m.put(SortOrder.class, SortOrderEnumStringInstantiator.DEFAULT);
        m.put(TableOrder.class, TableOrderEnumStringInstantiator.DEFAULT);
        m.put(TextAnchor.class, TextAnchorValueStringInstantiator.DEFAULT);

        return m;
    }

    public static final CommonInstantiatorsMap createDefaultInstantiatorsMap ()
    {
        return updateDefaultInstantiatorsMap(new CommonInstantiatorsMap());
    }

    private static Map<String,ValueStringInstantiator<?>>    _instMap    /* =null */;
    // CAVEAT EMPTOR
    public static final synchronized Map<String,ValueStringInstantiator<?>> getConvertersMap ()
    {
        if (null == _instMap)
            _instMap = createDefaultInstantiatorsMap();
        return _instMap;
    }
    // returns previous instance
    public static final synchronized Map<String,ValueStringInstantiator<?>> setConvertersMap (Map<String,ValueStringInstantiator<?>> m)
    {
        final Map<String,ValueStringInstantiator<?>>    prev=_instMap;
        _instMap = m;
        return prev;
    }

    public static <V> ValueStringInstantiator<V> getConverter (final Class<V> c)
    {
        if (null == c)
            return null;

        final Map<String,ValueStringInstantiator<?>>    cMap=getConvertersMap();
        if ((null == cMap) || (cMap.size() <= 0))
            return null;

        synchronized(cMap)
        {
            @SuppressWarnings("unchecked")
            final ValueStringInstantiator<V>    vsi=
                (ValueStringInstantiator<V>) cMap.get(c);
            return vsi;
        }
    }
}
