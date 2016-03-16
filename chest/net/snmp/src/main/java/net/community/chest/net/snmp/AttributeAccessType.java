/*
 *
 */
package net.community.chest.net.snmp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 25, 2009 2:14:17 PM
 */
public enum AttributeAccessType {
    READONLY("READ-ONLY", true, false),
    WRITEONLY("WRITE-ONLY", false, true),
    READWRITE("READ-WRITE", true, true);

    private final String    _str;
    /*
     * @see java.lang.Enum#toString()
     */
    @Override
    public final String toString ()
    {
        return _str;
    }

    private final boolean    _rd;
    public final boolean isReadable ()
    {
        return _rd;
    }

    private final boolean    _wr;
    public final boolean isWriteable ()
    {
        return _wr;
    }

    AttributeAccessType (String s, boolean rd, boolean wr)
    {
        _str = s;
        _rd = rd;
        _wr = wr;
    }

    public static final List<AttributeAccessType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final AttributeAccessType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final boolean isReadable (final String access)
    {
        return READONLY.toString().equalsIgnoreCase(access)
            || READWRITE.toString().equalsIgnoreCase(access);
    }

    public static final boolean isWriteable (final String access)
    {
        return READWRITE.toString().equalsIgnoreCase(access)
            || WRITEONLY.toString().equalsIgnoreCase(access);
    }
}
