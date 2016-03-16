/*
 *
 */
package net.community.chest.swing.component.progress;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingConstants;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 3, 2008 4:07:46 PM
 */
public enum BarOrientation {
    HORIZONTAL(SwingConstants.HORIZONTAL),
    VERTICAL(SwingConstants.VERTICAL);

    private final int    _o;
    public final int getOrientation ()
    {
        return _o;
    }

    BarOrientation (final int o)
    {
        _o = o;
    }

    public static final List<BarOrientation>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final BarOrientation fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final BarOrientation fromOrientation (final int o)
    {
        for (final BarOrientation v : VALUES)
        {
            if ((v != null) && (v.getOrientation() == o))
                return v;
        }

        return null;
    }
}
