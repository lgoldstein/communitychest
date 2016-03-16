/*
 *
 */
package net.community.chest.ui.helpers.resources;

import java.awt.Image;
import java.net.URL;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.community.chest.awt.image.BMPReader;
import net.community.chest.awt.image.ICOReader;
import net.community.chest.swing.resources.BaseUIAnchoredResourceAccessor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 6, 2009 11:46:10 AM
 */
public class HelperUIAnchoredResourceAccessor extends BaseUIAnchoredResourceAccessor {
    public HelperUIAnchoredResourceAccessor ()
    {
        super();
    }
    /*
     * @see net.community.chest.swing.resources.BaseUIAnchoredResourceAccessor#getIcon(java.net.URL)
     */
    @Override
    public Icon getIcon (URL iconURL) throws Exception
    {
        if (ICOReader.isIconFile(iconURL))
        {
            final List<? extends Image>    il=ICOReader.parseICOImages(iconURL);
            final int                    numImages=(null == il) ? 0 : il.size();
            final Image                    img=(numImages <= 0) ? null : il.get(0);
            if (null == img)
                return null;

            return new ImageIcon(img);
        }

        return super.getIcon(iconURL);
    }
    /*
     * @see net.community.chest.swing.resources.BaseUIAnchoredResourceAccessor#getImage(java.net.URL)
     */
    @Override
    public Image getImage (URL imgURL) throws Exception
    {
        if (BMPReader.isBitmapFile(imgURL))
        {
            final BMPReader                r=new BMPReader();
            final List<? extends Image>    il=r.readImages(imgURL);
            if ((null == il) || (il.size() <= 0))
                return null;

            return il.get(0);
        }

        return super.getImage(imgURL);
    }
}
