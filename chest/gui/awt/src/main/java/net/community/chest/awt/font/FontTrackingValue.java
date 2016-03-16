/*
 *
 */
package net.community.chest.awt.font;

import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulate pre-defined {@link TextAttribute#TRACKING} values</P>
 *
 * @author Lyor G.
 * @since Jun 17, 2009 4:07:15 PM
 */
public enum FontTrackingValue implements FontFloatAttributeValue {
    TIGHT(TextAttribute.TRACKING_TIGHT),
    LOOSE(TextAttribute.TRACKING_LOOSE);

    private final Float    _v;
    /*
     * @see net.community.chest.awt.font.FontFloatAttributeValue#getAttributeValue()
     */
    @Override
    public final Float getAttributeValue ()
    {
        return _v;
    }

    FontTrackingValue (Float v)
    {
        _v = v;
    }

    public static final List<FontTrackingValue>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final FontTrackingValue fromString (final String name)
    {
        return CollectionsUtils.fromString(VALUES, name, false);
    }

    public static final FontTrackingValue fromValue (final float f)
    {
        if (Float.isInfinite(f) || Float.isNaN(f))
            return null;

        return FontUtils.fromAttributeValue(f, VALUES);
    }

    public static final FontTrackingValue fromValue (final Float f)
    {
        return (null == f) ? null : fromValue(f.floatValue());
    }
}
