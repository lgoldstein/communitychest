/*
 * 
 */
package net.community.chest.swing.event;

import java.util.Collection;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.community.chest.awt.event.EventListenerSet;
import net.community.chest.awt.event.ListenerEventEnumUtils;
import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Uses the {@link ChangeListenerInstanceComparator#DEFAULT}</P>
 * @author Lyor G.
 * @since Jan 6, 2009 11:11:10 AM
 */
public class ChangeListenerSet extends EventListenerSet<ChangeListener> implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2004852990159695911L;

	public ChangeListenerSet ()
	{
		super(ChangeListenerInstanceComparator.DEFAULT);
	}

	public static final int fireChangeEvent (
			final ChangeEvent e, final Collection<? extends ChangeListener> ol, final boolean useCopy)
	{
		return ListenerEventEnumUtils.fireEvent(ChangeListenerEnum.DEFAULT, e, ol, useCopy);
	}

	public static final int fireChangeEvent (final ChangeEvent e, final ChangeListener ... cl)
	{
		if ((null == e) || (null == cl) || (cl.length <= 0))
			return 0;

		return fireChangeEvent(e, SetsUtils.uniqueSetOf(cl), false);
	}
	/**
	 * Fires a {@link ChangeEvent} using the provided source object for all the
	 * {@link ChangeListener}-s
	 * @param src The source {@link Object} - if <code>null</code> then no
	 * event is fired
	 * @param cl A {@link Collection} of {@link ChangeListener}-s to be
	 * invoked with the event
	 * @param useCopy <code>true</code> if create a copy of the {@link Collection}
	 * before traversing it. This should be used where the {@link Collection}
	 * might change during the invocation of a listener (e.g., if the listener
	 * removes itself) in order to avoid concurrent modification exception
	 * @return Number of invoked listeners (non-positive if none invoked)
	 */
	public static final int fireChangeEventForSource (
		final Object src, final Collection<? extends ChangeListener> cl, final boolean useCopy)
	{
		final int	numListeners=(null == cl) ? 0 : cl.size();
		if ((null == src) || (numListeners <= 0))
			return 0;

		return fireChangeEvent(new ChangeEvent(src), cl, useCopy);
	}

	public static final int fireChangeEventForSource (final Object src, final ChangeListener ... cl)
	{
		if ((null == src) || (null == cl) || (cl.length <= 0))
			return 0;

		return fireChangeEventForSource(src, SetsUtils.uniqueSetOf(cl), false);
	}
	/*
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged (ChangeEvent e)
	{
		fireChangeEvent(e, this, isUseCopyToFireEvents());
	}

	public void objectStateChanged (final Object src)
	{
		fireChangeEventForSource(src, this, isUseCopyToFireEvents());
	}
}
