/*
 *
 */
package net.community.chest.jms;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jms.Session;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates the <code>int</code> values as an {@link Enum}
 * @author Lyor G.
 * @since Sep 2, 2008 8:52:35 AM
 */
public enum SessionAckMode {
    AUTO(Session.AUTO_ACKNOWLEDGE),
    CLIENT(Session.CLIENT_ACKNOWLEDGE),
    DUPSOK(Session.DUPS_OK_ACKNOWLEDGE),
    TRANSACTED(Session.SESSION_TRANSACTED);

    private final int    _mode;
    public final int getMode ()
    {
        return _mode;
    }

    SessionAckMode (final int mode)
    {
        _mode = mode;
    }

    public static final List<SessionAckMode>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static SessionAckMode fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static SessionAckMode fromMode (final int mode)
    {
        for (final SessionAckMode v : VALUES)
        {
            if ((v != null) && (v.getMode() == mode))
                return v;
        }

        return null;
    }
}
