/*
 *
 */
package net.community.chest.awt.event;

import java.awt.AWTEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 4, 2009 12:22:32 PM
 */
public final class ListenerEventEnumUtils {
    private ListenerEventEnumUtils ()
    {
        // no instance
    }

    public static final <L extends EventListener,E extends EventObject,V extends ListenerEventEnum<L,E>> V fromEventId (
            final int id, final Collection<? extends V> vals)
    {
        if ((null == vals) || (vals.size() <= 0))
            return null;

        for (final V v : vals)
        {
            if ((v != null) && (v.getEventId() == id))
                return v;
        }

        return null;    // no match found
    }

    public static final <L extends EventListener,E extends AWTEvent,V extends ListenerEventEnum<L,E>> V fromEventId (
            final AWTEvent e, final Collection<? extends V> vals)
    {
        return (null == e) ? null : fromEventId(e.getID(), vals);
    }
    /**
     * Fires a {@link EventObject} derived event to all the {@link EventListener}-s
     * @param <L> Type of {@link EventListener}
     * @param <E> Type of {@link EventObject} expected as input to the listener's method
     * @param <V> Type of {@link ListenerEventEnum} used to invoke the method
     * @param t Type of invocation to be {@link ListenerEventEnum#invoke(EventListener, EventObject)}-d
     * @param e The event to be fired - if <code>null</code> then no
     * event is fired
     * @param ol A {@link Collection} of {@link EventListener}-s to be
     * invoked with the event
     * @param useCopy <code>true</code> if create a copy of the {@link Collection}
     * before traversing it. This should be used where the {@link Collection}
     * might change during the invocation of a listener (e.g., if the listener
     * removes itself) in order to avoid concurrent modification exception
     * @return Number of invoked listeners (non-positive if none invoked)
     */
    public static final <L extends EventListener,E extends EventObject,V extends ListenerEventEnum<L,E>> int fireEvent (
            final V t, final E e, final Collection<? extends L> ol, final boolean useCopy)
    {
        final int    numListeners=(null == ol) ? 0 : ol.size();
        if ((null == t) || (null == e) || (numListeners <= 0))
            return 0;

        final Collection<? extends L> cl;
        if (useCopy)
        {
            synchronized(ol)
            {
                cl = new ArrayList<L>(ol);
            }
        }
        else
            cl = ol;

        int    numInformed=0;
        for (final L l : cl)
        {
            if (null == l)
                continue;

            t.invoke(l, e);
            numInformed++;
        }

        return numInformed;
    }
}
