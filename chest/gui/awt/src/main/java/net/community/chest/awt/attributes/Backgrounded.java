/*
 *
 */
package net.community.chest.awt.attributes;

import java.awt.Color;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 30, 2008 8:36:56 AM
 */
public interface Backgrounded {
    public static final String        ATTR_NAME="background";
    public static final Class<?>    ATTR_TYPE=Color.class;

    Color getBackground ();
    void setBackground (Color c);
}
