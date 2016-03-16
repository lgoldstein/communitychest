/*
 *
 */
package net.community.chest.awt.event;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.community.chest.awt.AWTUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2009 8:19:14 AM
 */
public final class EventQueueUtils {
    private EventQueueUtils ()
    {
        // no instance
    }

    private static AtomicInteger    _AWTEventIDMapper;
    /**
     * Used to assign a unique {@link AWTEvent} ID on every call. This is the
     * best way to assure that derived/proprietary events have their own
     * ID - e.g., <code>public static final int MY_ID=AWTUtils.getAWTReservedEventId();</code>
     * @return A unique value that does not "clash" with others
     * @see AWTEvent#RESERVED_ID_MAX
     */
    public static final int getAWTReservedEventId ()
    {
        synchronized(AWTUtils.class)
        {
            if (null == _AWTEventIDMapper)
                _AWTEventIDMapper = new AtomicInteger(AWTEvent.RESERVED_ID_MAX);
        }

        return _AWTEventIDMapper.incrementAndGet();
    }

    private static EventQueue    _evq;
    /**
     * @return Default system {@link EventQueue} (unless preceded by a call
     * to {@link #setEventQueue(EventQueue)})
     * @see Toolkit#getSystemEventQueue()
     */
    public static final synchronized EventQueue getEventQueue ()
    {
        if (null == _evq)
            _evq = Toolkit.getDefaultToolkit().getSystemEventQueue();
        return _evq;
    }
    // returns previous instance
    public static final synchronized EventQueue setEventQueue (final EventQueue q)
    {
        final EventQueue    prev=_evq;
        _evq = q;
        return prev;
    }
    /**
     * @param <E> Type of {@link AWTEvent} to post
     * @param event Event to post - ignored if null
     * @return Same as input
     */
    public static final <E extends AWTEvent> E postEvent (final E event)
    {
        if (null == event)
            return event;

        final EventQueue    q=getEventQueue();
        q.postEvent(event);
        return event;
    }
    // NOTE !!! should call "enableEvents(eventId)" on the target component and override "processEvent"
    public static final UserDefinedEvent postUserDefinedEvent (Component target /* not null */, int eventId, List<?> args) throws IllegalArgumentException
    {
        return postEvent(new UserDefinedEvent(target, eventId, args));
    }
    // NOTE !!! should call "enableEvents(eventId)" on the target component and override "processEvent"
    public static final UserDefinedEvent postUserDefinedEvent (Component target /* not null */, int eventId, Object ... args) throws IllegalArgumentException
    {
        return postUserDefinedEvent(target, eventId, ((null == args) || (args.length <= 0)) ? null : Arrays.asList(args));
    }
}
