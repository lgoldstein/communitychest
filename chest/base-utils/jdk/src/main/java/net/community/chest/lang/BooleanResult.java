/*
 *
 */
package net.community.chest.lang;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to encapsulate the 3 possible {@link Boolean} values ({@link Boolean#TRUE},
 * {@link Boolean#FALSE} and <code>null</code></P>
 *
 * @author Lyor G.
 * @since Dec 30, 2008 7:40:04 AM
 */
public enum BooleanResult {
    NONE(null),
    TRUE(Boolean.TRUE),
    FALSE(Boolean.FALSE);

    private final Boolean    _res;
    public final Boolean getResult ()
    {
        return _res;
    }

    BooleanResult (Boolean res)
    {
        _res = res;
    }

    public static final List<BooleanResult>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final BooleanResult fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final BooleanResult fromPrimitiveValue (final boolean v)
    {
        return v ? TRUE : FALSE;
    }

    public static final BooleanResult fromTypeValue (final Boolean v)
    {
        return (null == v) ? NONE : fromPrimitiveValue(v.booleanValue());
    }
}
