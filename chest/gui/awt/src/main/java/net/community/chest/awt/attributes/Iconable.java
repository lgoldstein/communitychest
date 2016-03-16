/*
 *
 */
package net.community.chest.awt.attributes;

import java.awt.Dimension;

import javax.swing.Icon;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 30, 2008 8:33:59 AM
 */
public interface Iconable {
    public static final String        ATTR_NAME="icon";
    public static final Class<?>    ATTR_TYPE=Icon.class;

    public static final int    DEFAULT_HEIGHT=16, DEFAULT_WIDTH=DEFAULT_HEIGHT;
    public static final Dimension    DEFAULT_SIZE=new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);

    Icon getIcon ();
    void setIcon (Icon i);
}
