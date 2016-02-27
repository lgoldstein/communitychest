/*
 * 
 */
package net.community.chest.awt.attributes;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 30, 2008 8:34:29 AM
 */
public interface Tooltiped {
	public static final String		ATTR_NAME="toolTipText";
	public static final Class<?>	ATTR_TYPE=String.class;

	String getToolTipText ();
	void setToolTipText (String t);
}
