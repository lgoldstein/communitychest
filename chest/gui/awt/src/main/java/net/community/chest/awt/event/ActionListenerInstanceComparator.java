/*
 * 
 */
package net.community.chest.awt.event;

import java.awt.event.ActionListener;

import net.community.chest.util.compare.InstancesComparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 6, 2009 11:08:46 AM
 */
public class ActionListenerInstanceComparator extends InstancesComparator<ActionListener> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7361363898080014903L;

	public ActionListenerInstanceComparator ()
	{
		super(ActionListener.class);
	}

	public static final ActionListenerInstanceComparator	DEFAULT=new ActionListenerInstanceComparator();
}
