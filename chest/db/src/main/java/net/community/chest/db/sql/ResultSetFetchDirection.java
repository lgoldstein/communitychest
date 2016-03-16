/*
 *
 */
package net.community.chest.db.sql;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.EnumUtil;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Encapsulate the possible fetch direction values as an {@link Enum}</P>
 * @author Lyor G.
 * @since May 19, 2011 10:33:13 AM
 */
public enum ResultSetFetchDirection {
    FORWARD(ResultSet.FETCH_FORWARD),
    REVERSE(ResultSet.FETCH_REVERSE),
    UNKNOWN(ResultSet.FETCH_UNKNOWN);

    private final int    _direction;
    public final int getDirection ()
    {
        return _direction;
    }

    ResultSetFetchDirection (int direction)
    {
        _direction = direction;
    }

    public static final List<ResultSetFetchDirection>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final ResultSetFetchDirection fromString (final String name)
    {
        return EnumUtil.fromName(VALUES, name, false);
    }

    public static final ResultSetFetchDirection fromDirection (final int direction)
    {
        for (final ResultSetFetchDirection d : VALUES)
        {
            if ((d != null) && (d.getDirection() == direction))
                return d;
        }

        return null;
    }
}
