/*
 * 
 */
package net.community.chest.awt.attributes;


/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 30, 2008 8:41:00 AM
 */
public interface Enabled {
	public static final String		ATTR_NAME="enabled";
	public static final Class<?>	ATTR_TYPE=Boolean.TYPE;

	boolean isEnabled ();
	void setEnabled (boolean b);
}
