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
 * <P>Encapsulate well-known constant for {@link TextAttribute#JUSTIFICATION}</P>
 *
 * @author Lyor G.
 * @since Jun 18, 2009 8:15:48 AM
 */
public enum FontJustificationValue implements FontFloatAttributeValue {
    FULL(TextAttribute.JUSTIFICATION_FULL),
    NONE(TextAttribute.JUSTIFICATION_NONE);

    private final Float    _v;
    /*
     * @see net.community.chest.awt.font.FontFloatAttributeValue#getAttributeValue()
     */
    @Override
    public final Float getAttributeValue ()
    {
        return _v;
    }

    FontJustificationValue (Float v)
    {
        _v = v;
    }

    public static final List<FontJustificationValue>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final FontJustificationValue fromString (final String name)
    {
        return CollectionsUtils.fromString(VALUES, name, false);
    }

    public static final FontJustificationValue fromValue (final float f)
    {
        if (Float.isInfinite(f) || Float.isNaN(f))
            return null;

        return FontUtils.fromAttributeValue(f, VALUES);
    }

    public static final FontJustificationValue fromValue (final Float f)
    {
        return (null == f) ? null : fromValue(f.floatValue());
    }
}
