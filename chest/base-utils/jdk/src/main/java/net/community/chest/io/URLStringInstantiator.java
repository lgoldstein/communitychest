/*
 *
 */
package net.community.chest.io;

import java.net.URL;

import net.community.chest.reflect.ValueStringConstructor;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 7, 2009 10:39:04 AM
 */
public class URLStringInstantiator extends ValueStringConstructor<URL> {
    public URLStringInstantiator () throws IllegalArgumentException
    {
        super(URL.class);
    }

    public static final URLStringInstantiator    DEFAULT=new URLStringInstantiator();
}
