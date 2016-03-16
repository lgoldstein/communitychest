package net.community.chest.io.encode.hex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 16, 2008 3:36:51 PM
 */
public enum HexDumpPrecision {
    P0000(0),    // special value
    P0008(8),
    P0016(16),
    P0032(32),
    P0064(64),
    P0128(128),
    P0256(256),
    P0512(512),
    P1024(1024);

    private final int    _width;
    public final int getWidth ()
    {
        return _width;
    }

    HexDumpPrecision (int w)
    {
        _width = w;
    }

    public static final List<HexDumpPrecision>    VALUES=
        Collections.unmodifiableList(Arrays.asList(values()));

    public static final HexDumpPrecision fromWidth (final int w)
    {
        if (w < 0)
            return null;

        for (final HexDumpPrecision    v : VALUES)
        {
            if ((v != null) && (v.getWidth() == w))
                return v;
        }

        return null;
    }
}
