/*
 *
 */
package net.community.chest.awt.event;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Represents the possible actions for a {@link KeyListener}
 * @author Lyor G.
 * @since Apr 13, 2009 10:13:13 AM
 */
public enum KeyEventType implements ListenerEventEnum<KeyListener,KeyEvent> {
    TYPED(KeyEvent.KEY_TYPED) {
            /*
             * @see net.community.chest.awt.event.ListenerEventEnum#invoke(java.util.EventListener, java.awt.AWTEvent)
             */
            @Override
            public void invoke (KeyListener l, KeyEvent e)
            {
                if ((null == l) || (null == e))
                    return;    // debug breakpoint
                l.keyTyped(e);
            }
        },
    PRESSED(KeyEvent.KEY_PRESSED) {
            /*
             * @see net.community.chest.awt.event.ListenerEventEnum#invoke(java.util.EventListener, java.awt.AWTEvent)
             */
            @Override
            public void invoke (KeyListener l, KeyEvent e)
            {
                if ((null == l) || (null == e))
                    return;    // debug breakpoint
                l.keyPressed(e);
            }
        },
    RELEASED(KeyEvent.KEY_RELEASED) {
            /*
             * @see net.community.chest.awt.event.ListenerEventEnum#invoke(java.util.EventListener, java.awt.AWTEvent)
             */
            @Override
            public void invoke (KeyListener l, KeyEvent e)
            {
                if ((null == l) || (null == e))
                    return;    // debug breakpoint
                l.keyReleased(e);
            }
        };

    private final int    _eventId;
    /*
     * @see net.community.chest.awt.event.ListenerEventEnum#getEventId()
     */
    @Override
    public final int getEventId ()
    {
        return _eventId;
    }
    /*
     * @see net.community.chest.awt.event.ListenerEventEnum#getEventClass()
     */
    @Override
    public final Class<KeyEvent> getEventClass ()
    {
        return KeyEvent.class;
    }
    /*
     * @see net.community.chest.awt.event.ListenerEventEnum#getListenerClass()
     */
    @Override
    public final Class<KeyListener> getListenerClass ()
    {
        return KeyListener.class;
    }

    KeyEventType (int eventId)
    {
        _eventId = eventId;
    }

    public static final List<KeyEventType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final KeyEventType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final KeyEventType fromEventId (final int id)
    {
        return ListenerEventEnumUtils.fromEventId(id, VALUES);
    }

    public static final KeyEventType fromEventId (final KeyEvent e)
    {
        return (null == e) ? null : fromEventId(e.getID());
    }
}
