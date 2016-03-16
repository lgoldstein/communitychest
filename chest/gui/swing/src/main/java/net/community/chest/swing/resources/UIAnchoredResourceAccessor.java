/*
 *
 */
package net.community.chest.swing.resources;

import java.awt.Image;
import java.net.URL;

import javax.swing.Icon;

import net.community.chest.resources.AnchoredResourceAccessor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 21, 2008 1:27:52 PM
 */
public interface UIAnchoredResourceAccessor extends AnchoredResourceAccessor {
    /**
     * @param name Resource name
     * @return Loaded {@link Icon} - null if resources does not exist
     * @throws Exception If failed to load the image
     */
    Icon getIcon (String name) throws Exception;
    Icon getIcon (URL iconURL) throws Exception;
    Image getImage (String name) throws Exception;
    Image getImage (URL imgURL) throws Exception;
}
