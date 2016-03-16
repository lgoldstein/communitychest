/*
 *
 */
package net.community.chest.awt.attributes;

import java.awt.Font;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 31, 2008 8:12:52 AM
 */
public interface FontControl {
    public static final String        ATTR_NAME="font";
    public static final Class<?>    ATTR_TYPE=Font.class;

    Font getFont ();
    void setFont (Font f);
}
