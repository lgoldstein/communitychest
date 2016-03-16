/*
 *
 */
package net.community.chest.apache.ant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.apache.tools.ant.Project;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provide an {@link Enum} encapsulating the <code><I>int</I></code> values</P>
 *
 * @author Lyor G.
 * @since Aug 28, 2008 11:58:51 AM
 */
public enum LogLevels {
    ERROR(Project.MSG_ERR),
    WARN(Project.MSG_WARN),
    INFO(Project.MSG_INFO),
    VERBOSE(Project.MSG_VERBOSE),
    DEBUG(Project.MSG_DEBUG);

    private final int    _level;
    public final int getLevel ()
    {
        return _level;
    }

    LogLevels (final int level)
    {
        _level = level;
    }

    public static final List<LogLevels>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final LogLevels fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final LogLevels fromLevel (final int level)
    {
        for (final LogLevels v : VALUES)
        {
            if ((v != null) && (v.getLevel() == level))
                return v;
        }

        return null;
    }
}
