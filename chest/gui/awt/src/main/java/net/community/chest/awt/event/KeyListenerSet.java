/*
 * 
 */
package net.community.chest.awt.event;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;

import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Uses the {@link KeyListenerInstanceComparator#DEFAULT} instance</P>
 * 
 * @author Lyor G.
 * @since Jan 6, 2009 11:14:13 AM
 */
public class KeyListenerSet extends EventListenerSet<KeyListener> implements KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7613432757875258961L;
	public KeyListenerSet ()
	{
		super(KeyListenerInstanceComparator.DEFAULT);
	}

	public static final int fireKeyEvent (
			final KeyEventType t, final KeyEvent e, final Collection<? extends KeyListener> ol, final boolean useCopy)
	{
		return ListenerEventEnumUtils.fireEvent(t, e, ol, useCopy);
	}

	public static final int fireKeyEvent (
			final KeyEventType t, final KeyEvent e, final KeyListener ... cl)
	{
		if ((null == e) || (null == cl) || (cl.length <= 0))
			return 0;

		return fireKeyEvent(t, e, SetsUtils.uniqueSetOf(cl), false);
	}
	/*
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyPressed (KeyEvent e)
	{
		fireKeyEvent(KeyEventType.PRESSED, e, this, isUseCopyToFireEvents());
	}
	/*
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyReleased (KeyEvent e)
	{
		fireKeyEvent(KeyEventType.RELEASED, e, this, isUseCopyToFireEvents());
	}
	/*
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyTyped (KeyEvent e)
	{
		fireKeyEvent(KeyEventType.TYPED, e, this, isUseCopyToFireEvents());
	}
}
