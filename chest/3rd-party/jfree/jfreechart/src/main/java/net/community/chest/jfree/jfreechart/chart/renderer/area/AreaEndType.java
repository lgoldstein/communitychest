/*
 *
 */
package net.community.chest.jfree.jfreechart.chart.renderer.area;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.renderer.AreaRendererEndType;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulates {@link AreaRendererEndType} values as {@link Enum}-s</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2009 1:51:24 PM
 */
public enum AreaEndType {
    TAPER(AreaRendererEndType.TAPER),
    TRUNCATE(AreaRendererEndType.TRUNCATE),
    LEVEL(AreaRendererEndType.LEVEL);

    private final AreaRendererEndType    _type;
    public final AreaRendererEndType getType ()
    {
        return _type;
    }

    AreaEndType (AreaRendererEndType t)
    {
        _type = t;
    }

    public static final List<AreaEndType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final AreaEndType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final AreaEndType fromType (final AreaRendererEndType t)
    {
        if (null == t)
            return null;

        for (final AreaEndType  v : VALUES)
        {
            if ((v != null) && t.equals(v.getType()))
                return v;
        }

        return null;
    }
}
