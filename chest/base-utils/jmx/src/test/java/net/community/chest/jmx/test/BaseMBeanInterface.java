package net.community.chest.jmx.test;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 16, 2007 12:34:44 PM
 */
public interface BaseMBeanInterface {
    /**
     * @return internal string value
     */
    String getStringValue ();
    void setStringValue (String v);
    /**
     * Does nothing
     */
    void doVoidOperation ();
}
