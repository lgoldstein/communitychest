/*
 * 
 */
package net.community.chest.awt.attributes;

import java.awt.Color;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 30, 2008 8:38:46 AM
 */
public interface Foregrounded {
	public static final String		ATTR_NAME="foreground";
	public static final Class<?>	ATTR_TYPE=Color.class;

	Color getForeground ();
	void setForeground (Color c);
}
