/*
 * 
 */
package net.community.chest.awt.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Uses the {@link ActionListenerInstanceComparator#DEFAULT}</P>
 * 
 * @author Lyor G.
 * @since Jan 6, 2009 11:12:21 AM
 */
public class ActionListenerSet extends EventListenerSet<ActionListener> implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 195570162730692519L;
	public ActionListenerSet ()
	{
		super(ActionListenerInstanceComparator.DEFAULT);
	}

	public static final int fireActionEvent (
			final ActionEvent e, final Collection<? extends ActionListener> ol, final boolean useCopy)
	{
		return ListenerEventEnumUtils.fireEvent(ActionListenerEnum.DEFAULT, e, ol, useCopy);
	}

	public static final int fireActionEvent (final ActionEvent e, final ActionListener ... cl)
	{
		if ((null == e) || (null == cl) || (cl.length <= 0))
			return 0;

		return fireActionEvent(e, SetsUtils.uniqueSetOf(cl), false);
	}
	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed (ActionEvent e)
	{
		fireActionEvent(e, this, isUseCopyToFireEvents());
	}
}
