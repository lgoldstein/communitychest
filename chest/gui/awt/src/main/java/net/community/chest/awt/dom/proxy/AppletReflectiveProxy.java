/*
 *
 */
package net.community.chest.awt.dom.proxy;

import java.applet.Applet;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <A> The reflected {@link Applet} type
 * @author Lyor G.
 * @since Feb 16, 2009 12:53:12 PM
 */
public class AppletReflectiveProxy<A extends Applet> extends PanelReflectiveProxy<A> {
    protected AppletReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public AppletReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

}
