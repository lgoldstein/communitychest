/*
 *
 */
package net.community.chest.swing.event;

import java.util.EventListener;

import javax.swing.event.EventListenerList;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 12, 2009 2:08:26 PM
 */
public final class EventListenerListUtils {
    private EventListenerListUtils ()
    {
        // no instance
    }

    public static final <L extends EventListener> boolean contains (final EventListenerList ll, final Class<L> lc, final L el)
    {
        if ((null == ll) || (ll.getListenerCount() <= 0) || (null == el))
            return false;

        final Object[]    la=(null == lc) ? ll.getListenerList() : ll.getListeners(lc);
        if ((null == la) || (la.length <= 0))
            return false;

        for (final Object    oa : la)
        {
            final Class<?>    oc=(null == oa) ? null : oa.getClass();
            if (null == oc)
                continue;
            if (oa == el)    // just in case
                return true;

            if (!Object[].class.isAssignableFrom(oc))
                continue;

            for (final Object ol : (Object[]) oa)
            {
                if (ol == el)
                    return true;
            }
        }

        return false;
    }

    public static final boolean contains (final EventListenerList ll, final EventListener el)
    {
        return contains(ll, null, el);
    }
}
