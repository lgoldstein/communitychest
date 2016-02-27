/*
 * 
 */
package net.community.chest.awt.attributes;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 31, 2008 12:39:03 PM
 */
public interface Titled {
	public static final String		ATTR_NAME="title";
	public static final Class<?>	ATTR_TYPE=String.class;

	String getTitle ();
	void setTitle (String t);
}
