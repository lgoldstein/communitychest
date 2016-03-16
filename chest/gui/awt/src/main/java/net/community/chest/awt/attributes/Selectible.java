/*
 *
 */
package net.community.chest.awt.attributes;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 15, 2009 3:37:31 PM
 */
public interface Selectible {
    public static final String        ATTR_NAME="selected";
    public static final Class<?>    ATTR_TYPE=Boolean.TYPE;

    boolean isSelected ();
    void setSelected (boolean v);
}
