/*
 * 
 */
package net.community.chest.awt;

import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.dnd.DropTargetListener;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.EventListener;

/**
 * <P>Copyright as per GPLv2</P>
 * An {@link Enum} that encompasses some of the {@link EventListener} used in AWT objects
 * @author Lyor G.
 * @since Mar 20, 2011 1:11:52 PM
 */
public enum AWTListenerType {
	ACTION(ActionListener.class),
	ADJUSTMENT(AdjustmentListener.class),
	AWTEVENT(AWTEventListener.class),
	COMPONENT(ComponentListener.class),
	CONTAINER(ContainerListener.class),
	DRAGGESTURE(DragGestureListener.class),
	DRAGSOURCE(DragSourceListener.class),
	DRAGSOURCEMOTION(DragSourceMotionListener.class),
	DROPTARGET(DropTargetListener.class),
	FOCUS(FocusListener.class),
	KEY(KeyListener.class),
	MOUSE(MouseListener.class);

	private final Class<? extends EventListener>	_ltype;
	public final Class<? extends EventListener> getListenerType ()
	{
		return _ltype;
	}

	AWTListenerType (Class<? extends EventListener> ltype)
	{
		_ltype = ltype; 
	}
}
