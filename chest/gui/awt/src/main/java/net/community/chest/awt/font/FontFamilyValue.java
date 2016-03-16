/*
 *
 */
package net.community.chest.awt.font;

import java.awt.Font;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulate pre-defined constants for the {@link java.awt.font.TextAttribute#FAMILY}
 * {@link Font} attribute</P>
 *
 * @author Lyor G.
 * @since Jun 18, 2009 7:24:51 AM
 */
public enum FontFamilyValue {
    DIALOG(Font.DIALOG),
    DLGINPUT(Font.DIALOG_INPUT),
    SERIF(Font.SERIF),
    SANSERIF(Font.SANS_SERIF),
    MONOSPACED(Font.MONOSPACED);

    private final String    _f;
    public final String getAttributeValue ()
    {
        return _f;
    }

    FontFamilyValue (String f)
    {
        _f = f;
    }

    public static final List<FontFamilyValue>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final FontFamilyValue fromString (final String name)
    {
        return CollectionsUtils.fromString(VALUES, name, false);
    }

    public static final FontFamilyValue fromValue (final String n)
    {
        if ((null == n) || (n.length() <= 0))
            return null;

        for (final FontFamilyValue v : VALUES)
        {
            final String    vn=(null == v) ? null : v.getAttributeValue();
            if (n.equalsIgnoreCase(vn))
                return v;
        }

        return null;
    }
}
