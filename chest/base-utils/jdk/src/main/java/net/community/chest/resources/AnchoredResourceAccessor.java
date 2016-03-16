/*
 *
 */
package net.community.chest.resources;

import java.net.URL;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 21, 2008 12:29:01 PM
 */
public interface AnchoredResourceAccessor {
    /**
     * @param name The resource location
     * @return The {@link URL} of the resource as calculated from the actual
     * class implementing this method - null if resource does not exist
     * @see Class#getResource(String)
     */
    URL getResource (String name);
}
