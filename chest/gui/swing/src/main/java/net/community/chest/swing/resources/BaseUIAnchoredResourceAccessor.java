/*
 *
 */
package net.community.chest.swing.resources;

import java.awt.Image;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.community.chest.resources.BaseXmlAnchoredResourceAccessor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 21, 2008 1:31:03 PM
 */
public class BaseUIAnchoredResourceAccessor
            extends BaseXmlAnchoredResourceAccessor
            implements UIAnchoredResourceAccessor {
    public BaseUIAnchoredResourceAccessor ()
    {
        super();
    }
    /*
     * @see net.community.chest.swing.resources.UIAnchoredResourceAccessor#getIcon(java.net.URL)
     */
    @Override
    public Icon getIcon (final URL iconURL) throws Exception
    {
        if (null == iconURL)
            return null;

        return new ImageIcon(iconURL);
    }
    /*
     * @see net.community.chest.swing.resources.UIAnchoredResourceAccessor#getIcon(java.lang.String)
     */
    @Override
    public Icon getIcon (final String name) throws Exception
    {
        return getIcon(getResource(name));
    }
    /*
     * @see net.community.chest.swing.resources.UIAnchoredResourceAccessor#getImage(java.net.URL)
     */
    @Override
    public Image getImage (URL imgURL) throws Exception
    {
        final Icon    icon=getIcon(imgURL);
        if (null == icon)
            return null;

        if (icon instanceof ImageIcon)
            return ((ImageIcon) icon).getImage();

        throw new ClassCastException("getImage(" + imgURL + ") unknown icon type: " + icon.getClass().getName());
    }
    /*
     * @see net.community.chest.swing.resources.UIAnchoredResourceAccessor#getImage(java.lang.String)
     */
    @Override
    public Image getImage (String name) throws Exception
    {
        return getImage(getResource(name));
    }
}
