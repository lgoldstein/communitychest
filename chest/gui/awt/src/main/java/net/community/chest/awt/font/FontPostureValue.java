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
 * <P>Encapsulate well-known constant for {@link TextAttribute#POSTURE}</P>
 *
 * @author Lyor G.
 * @since Jun 18, 2009 7:56:01 AM
 */
public enum FontPostureValue implements FontFloatAttributeValue {
    REGULAR(TextAttribute.POSTURE_REGULAR),
    OBLIQUE(TextAttribute.POSTURE_OBLIQUE);

    private final Float    _v;
    /*
     * @see net.community.chest.awt.font.FontFloatAttributeValue#getAttributeValue()
     */
    @Override
    public final Float getAttributeValue ()
    {
        return _v;
    }

    FontPostureValue (Float v)
    {
        _v = v;
    }

    public static final List<FontPostureValue>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final FontPostureValue fromString (final String name)
    {
        return CollectionsUtils.fromString(VALUES, name, false);
    }

    public static final FontPostureValue fromValue (final float f)
    {
        if (Float.isInfinite(f) || Float.isNaN(f))
            return null;

        return FontUtils.fromAttributeValue(f, VALUES);
    }

    public static final FontPostureValue fromValue (final Float f)
    {
        return (null == f) ? null : fromValue(f.floatValue());
    }
}
