/*
 * 
 */
package net.community.chest.jmx;

import javax.management.ObjectName;

import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 15, 2011 9:01:01 AM
 */
public abstract class AbstractObjectNameComparator extends AbstractComparator<ObjectName> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5952340362354801922L;

	protected AbstractObjectNameComparator (boolean ascending)
	{
		super(ObjectName.class, !ascending);
	}
}
