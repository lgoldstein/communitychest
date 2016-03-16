/*
 *
 */
package net.community.chest.awt.attributes;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 22, 2009 11:01:17 AM
 */
public interface Editable {
    public static final String        ATTR_NAME="editable";
    public static final Class<?>    ATTR_TYPE=Boolean.TYPE;

    boolean isEditable ();
    void setEditable (boolean b);
}
