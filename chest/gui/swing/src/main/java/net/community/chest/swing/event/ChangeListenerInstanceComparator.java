/*
 * 
 */
package net.community.chest.swing.event;

import javax.swing.event.ChangeListener;

import net.community.chest.util.compare.InstancesComparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 6, 2009 11:03:57 AM
 */
public class ChangeListenerInstanceComparator extends InstancesComparator<ChangeListener> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3504504088793450512L;

	public ChangeListenerInstanceComparator ()
	{
		super(ChangeListener.class);
	}

	public static final ChangeListenerInstanceComparator	DEFAULT=new ChangeListenerInstanceComparator();
}
