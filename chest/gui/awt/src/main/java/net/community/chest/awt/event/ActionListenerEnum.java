/*
 * 
 */
package net.community.chest.awt.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

public enum ActionListenerEnum implements ListenerEventEnum<ActionListener,ActionEvent> {
	DEFAULT;
	/*
	 * @see net.community.chest.awt.event.ListenerEventEnum#getEventClass()
	 */
	@Override
	public final Class<ActionEvent> getEventClass ()
	{
		return ActionEvent.class;
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
	public final Class<ActionListener> getListenerClass ()
	{
		return ActionListener.class;
	}
	/*
	 * @see net.community.chest.awt.event.ListenerEventEnum#invoke(java.util.EventListener, java.awt.AWTEvent)
	 */
	@Override
	public void invoke (ActionListener l, ActionEvent e)
	{
		if ((null == l) || (null == e))
			return;	// debug breakpoint
		l.actionPerformed(e);
	}

	public static final List<ActionListenerEnum>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final ActionListenerEnum fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
}