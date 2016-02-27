/*
 * 
 */
package net.community.chest.awt.attributes;

import java.awt.Dimension;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 23, 2009 12:40:35 PM
 */
public interface Sizeable {
	public static final String		ATTR_NAME="size";
	public static final Class<?>	ATTR_TYPE=Dimension.class;

	Dimension getSize ();
	void setSize (Dimension d);
}
