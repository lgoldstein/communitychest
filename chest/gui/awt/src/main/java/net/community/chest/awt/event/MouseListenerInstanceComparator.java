/*
 * 
 */
package net.community.chest.awt.event;

import java.awt.event.MouseListener;

import net.community.chest.util.compare.InstancesComparator;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 4, 2009 12:52:45 PM
 */
public class MouseListenerInstanceComparator extends InstancesComparator<MouseListener> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7935492742566070712L;

	public MouseListenerInstanceComparator ()
	{
		super(MouseListener.class);
	}

	public static final MouseListenerInstanceComparator	DEFAULT=new MouseListenerInstanceComparator();
}
