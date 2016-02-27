/*
 * 
 */
package net.community.chest.awt.event;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 4, 2009 12:01:16 PM
 */
public enum MouseEventType implements ListenerEventEnum<MouseListener,MouseEvent> {
	CLICKED(MouseEvent.MOUSE_CLICKED) {
			/*
			 * @see net.community.chest.awt.event.ListenerEventEnum#invoke(java.util.EventListener, java.awt.AWTEvent)
			 */
			@Override
			public void invoke (MouseListener l, MouseEvent e)
			{
				if ((null == l) || (null == e))
					return;	// debug breakpoint
				l.mouseClicked(e);
			}
		},
	PRESSED(MouseEvent.MOUSE_PRESSED) {
			/*
			 * @see net.community.chest.awt.event.ListenerEventEnum#invoke(java.util.EventListener, java.awt.AWTEvent)
			 */
			@Override
			public void invoke (MouseListener l, MouseEvent e)
			{
				if ((null == l) || (null == e))
					return;	// debug breakpoint
				l.mousePressed(e);
			}
		},
	RELEASED(MouseEvent.MOUSE_RELEASED) {
			/*
			 * @see net.community.chest.awt.event.ListenerEventEnum#invoke(java.util.EventListener, java.awt.AWTEvent)
			 */
			@Override
			public void invoke (MouseListener l, MouseEvent e)
			{
				if ((null == l) || (null == e))
					return;	// debug breakpoint
				l.mouseReleased(e);
			}
		},
	ENTERED(MouseEvent.MOUSE_ENTERED) {
			/*
			 * @see net.community.chest.awt.event.ListenerEventEnum#invoke(java.util.EventListener, java.awt.AWTEvent)
			 */
			@Override
			public void invoke (MouseListener l, MouseEvent e)
			{
				if ((null == l) || (null == e))
					return;	// debug breakpoint
				l.mouseEntered(e);
			}
		},
	EXITED(MouseEvent.MOUSE_EXITED) {
			/*
			 * @see net.community.chest.awt.event.ListenerEventEnum#invoke(java.util.EventListener, java.awt.AWTEvent)
			 */
			@Override
			public void invoke (MouseListener l, MouseEvent e)
			{
				if ((null == l) || (null == e))
					return;	// debug breakpoint
				l.mouseExited(e);
			}
		};

	private final int	_eventId;
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
	public final Class<MouseEvent> getEventClass ()
	{
		return MouseEvent.class;
	}
	/*
	 * @see net.community.chest.awt.event.ListenerEventEnum#getListenerClass()
	 */
	@Override
	public final Class<MouseListener> getListenerClass ()
	{
		return MouseListener.class;
	}

	MouseEventType (int eventId)
	{
		_eventId = eventId;
	}

	public static final List<MouseEventType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final MouseEventType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final MouseEventType fromEventId (final int id)
	{
		return ListenerEventEnumUtils.fromEventId(id, VALUES);
	}

	public static final MouseEventType fromEventId (final MouseEvent e)
	{
		return (null == e) ? null : fromEventId(e.getID());
	}
}
