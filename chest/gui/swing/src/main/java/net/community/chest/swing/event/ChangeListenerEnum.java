/*
 *
 */
package net.community.chest.swing.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.community.chest.awt.event.ListenerEventEnum;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 4, 2009 1:10:48 PM
 */
public enum ChangeListenerEnum implements ListenerEventEnum<ChangeListener,ChangeEvent> {
    DEFAULT;
    /*
     * @see net.community.chest.awt.event.ListenerEventEnum#getEventClass()
     */
    @Override
    public final Class<ChangeEvent> getEventClass ()
    {
        return ChangeEvent.class;
    }
    /*
     * @see net.community.chest.awt.event.ListenerEventEnum#getEventId()
     */
    @Override
    public final int getEventId ()
    {
        return (-1);
    }
    /*
     * @see net.community.chest.awt.event.ListenerEventEnum#getListenerClass()
     */
    @Override
    public final Class<ChangeListener> getListenerClass ()
    {
        return ChangeListener.class;
    }
    /*
     * @see net.community.chest.awt.event.ListenerEventEnum#invoke(java.util.EventListener, java.awt.AWTEvent)
     */
    @Override
    public void invoke (ChangeListener l, ChangeEvent e)
    {
        if ((null == l) || (null == e))
            return;    // debug breakpoint
        l.stateChanged(e);
    }

    public static final List<ChangeListenerEnum>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final ChangeListenerEnum fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

}
