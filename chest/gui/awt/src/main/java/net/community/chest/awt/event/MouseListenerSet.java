/*
 * 
 */
package net.community.chest.awt.event;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 4, 2009 12:53:39 PM
 */
public class MouseListenerSet extends EventListenerSet<MouseListener> implements MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4280824403960070207L;
	public MouseListenerSet ()
	{
		super(MouseListenerInstanceComparator.DEFAULT);
	}

	public static final int fireMouseEvent (
			final MouseEventType t, final MouseEvent e, final Collection<? extends MouseListener> ol, final boolean useCopy)
	{
		return ListenerEventEnumUtils.fireEvent(t, e, ol, useCopy);
	}

	public static final int fireMouseEvent (
			final MouseEventType t, final MouseEvent e, final MouseListener ... cl)
	{
		if ((null == e) || (null == cl) || (cl.length <= 0))
			return 0;

		return fireMouseEvent(t, e, SetsUtils.uniqueSetOf(cl), false);
	}
	/*
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked (MouseEvent e)
	{
		fireMouseEvent(MouseEventType.CLICKED, e, this, isUseCopyToFireEvents());
	}
	/*
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered (MouseEvent e)
	{
		fireMouseEvent(MouseEventType.ENTERED, e, this, isUseCopyToFireEvents());
	}
	/*
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited (MouseEvent e)
	{
		fireMouseEvent(MouseEventType.EXITED, e, this, isUseCopyToFireEvents());
	}
	/*
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed (MouseEvent e)
	{
		fireMouseEvent(MouseEventType.PRESSED, e, this, isUseCopyToFireEvents());
	}
	/*
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased (MouseEvent e)
	{
		fireMouseEvent(MouseEventType.RELEASED, e, this, isUseCopyToFireEvents());
	}
}
