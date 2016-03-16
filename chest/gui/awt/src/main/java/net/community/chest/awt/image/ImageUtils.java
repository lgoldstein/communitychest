/*
 *
 */
package net.community.chest.awt.image;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.community.chest.awt.layout.border.BorderLayoutPosition;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 8, 2009 3:48:26 PM
 *
 */
public final class ImageUtils {
    private ImageUtils ()
    {
        // no instance
    }

    public static final Integer getHeight (final Image img)
    {
        if (img instanceof BufferedImage)
            return Integer.valueOf(((BufferedImage) img).getHeight());
        else if (ToolkitImageAccessor.isImageClass(img))
            return ToolkitImageAccessor.getHeight(img);
        else if (img != null)
            return Integer.valueOf(img.getHeight(null));
        else
            return null;
    }

    public static final Integer getWidth (final Image img)
    {
        if (img instanceof BufferedImage)
            return Integer.valueOf(((BufferedImage) img).getWidth());
        else if (ToolkitImageAccessor.isImageClass(img))
            return ToolkitImageAccessor.getWidth(img);
        else if (img != null)
            return Integer.valueOf(img.getWidth(null));
        else
            return null;
    }
    /**
     * Calculates the best possible bounds for drawing an image within the
     * specific bounds given its preferred position
     * @param imgSize The image {@link Dimension} - if <code>null</code> then
     * a <code>null</code> bounds value is returned
     * @param cntBounds The available display area {@link Rectangle}- if
     * <code>null</code> then a <code>null</code> bounds value is returned
     * @param p The preferred position within the display area (expressed as
     * a {@link BorderLayoutPosition} - if <code>null</code> then a
     * <code>null</code> bounds value is returned
     * @return The best {@link Rectangle} to be used - <code>null</code> if
     * not enough parameters or the image size/bounds are "empty" (i.e., zero
     * width/height)
     */
    public static final Rectangle getImageDrawBounds (
                                            final Dimension                imgSize,
                                            final Rectangle                cntBounds,
                                            final BorderLayoutPosition     p)
    {
        if ((null == imgSize) || (null == cntBounds) || (null == p))
            return null;

        // make sure values are non-negative
        final int    ow=Math.max(imgSize.width, 0),
                    oh=Math.max(imgSize.height, 0),
                    bw=Math.max(cntBounds.width, 0),
                    bh=Math.max(cntBounds.height, 0),
        // makes sure image width/height do not exceed available
                    iw=Math.min(ow, bw),
                    ih=Math.min(oh, bh);
        if ((iw <= 0) || (ih <= 0) || (bw <= 0) || (bh <= 0))
            return null;

        final Rectangle    r=new Rectangle(0, 0, iw, ih);
        switch(p)
        {
            case BEFORE_FIRST_LINE    :
            case PAGE_START            :    // top-left corner
                break;

            case SOUTH                :
                r.y = Math.max(bh - ih, 0);
                // fall through

            case NORTH                :
                r.x = Math.max(bw - iw, 0) / 2;
                break;

            case CENTER                :
                r.x = Math.max(bw - iw, 0) / 2;
                r.y = Math.max(bh - ih, 0) / 2;
                break;

            case LINE_END            :    // top-right corner
                r.x = Math.max(bw - iw, 0);
                break;

            case AFTER_LINE_ENDS    :
            case EAST                :
                r.x = Math.max(bw - iw, 0);
                // fall through

            case LINE_START            :
            case WEST                :
                r.y = Math.max(bh - ih, 0) / 2;
                break;

            case BEFORE_LINE_BEGINS    :    // bottom-left
                r.y = Math.max(bh - ih, 0);
                break;

            case AFTER_LAST_LINE    :
            case PAGE_END            :    // bottom-right corner
                r.x = Math.max(bw - iw, 0);
                r.y = Math.max(bh - ih, 0);
                break;

            default                    :
                return null;
        }

        // adjust for the margin (if any)
        r.x += cntBounds.x;
        r.y += cntBounds.y;
        return r;
    }
    /**
     * Draws an image within the specified bounds taking into account its
     * preferred position (expressed as a {@link BorderLayoutPosition})
     * @param g The {@link Graphics} context to use for drawing the image -
     * if <code>null</code> then nothing is drawn
     * @param i The {@link Image} to be drawn - if <code>null</code> then
     * nothing is drawn
     * @param p The preferred relative position within the display area - if
     * <code>null</code> then nothing is drawn
     * @param r The available display area {@link Rectangle} within the total
     * client area (i.e., {@link Rectangle#x},{@link Rectangle#y} are taken
     * into account when calculating the drawn image top-left corner). If the
     * available area is <code>null</code> or "empty" (i.e., zero
     * width/height) then nothing is drawn
     * @param o The {@link ImageObserver} instance to be passed to the
     * {@link Graphics#drawImage(Image, int, int, int, int, ImageObserver)}
     * method
     * @return The {@link Rectangle} used to enclose the drawn image -
     * <code>null</code> if nothing drawn
     */
    public static final Rectangle drawImage (
                                        final Graphics                 g,
                                        final Image                    i,
                                        final BorderLayoutPosition     p,
                                        final Rectangle                r,
                                        final ImageObserver            o)
    {
        if ((null == g) || (null == i) || (null == p) || (null == r))
            return null;

        final Integer    h=getHeight(i), w=getWidth(i);
        if ((null == h) || (null == w))
            return null;

        final Rectangle    dp=
            getImageDrawBounds(new Dimension(w.intValue(), h.intValue()), r, p);
        if ((null == dp) || (dp.width <= 0) || (dp.height <= 0))
            return null;

        g.drawImage(i, dp.x, dp.y, dp.width, dp.height, o);
        return dp;
    }
    /**
     * Attempts to <U>reduce</U> an {@link Icon}-s size to the preferred
     * width/height
     * @param icon The original {@link Icon} - ignored if <code>null</code>
     * @param width The required width - ignored if non-positive
     * @param height The required height - ignored if non-positive
     * @return The adjusted {@link Icon} - may be same as input if icon
     * dimensions already below the required ones and/or ignored parameters
     */
    public static final Icon adjustIconSize (
            final Icon icon, final int width, final int height)
    {
        if ((null == icon) || (width <= 0) || (height <= 0))
            return icon;

        final int    w=icon.getIconWidth(), h=icon.getIconHeight();
        if ((w <= width) && (h <= height))
            return icon;

        if (!(icon instanceof ImageIcon))
            return icon;

        final Image    org=((ImageIcon) icon).getImage(),
                    adj=org.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        if (adj != null)
            return new ImageIcon(adj);

        return icon;
    }

    public static final Image copyImage (final Image org, final int imgType)
    {
        if (null == org)
            return null;

        final Integer    h=getHeight(org), w=getWidth(org);
        if ((null == h) || (null == w))
            return org;

        final BufferedImage ret=
            new BufferedImage(w.intValue(), h.intValue(), imgType);
        final Graphics g = ret.getGraphics();
        g.drawImage(org, 0, 0, null);

        return ret;
    }

    public static final Image copyImage (final Image org)
    {
        return copyImage(org, (org instanceof BufferedImage) ? ((BufferedImage) org).getType() : BufferedImage.TYPE_INT_ARGB);
    }
    /**
     * @param iOrig Original {@link Image} instance - if <code>null</code>
     * then overlay instance is returned
     * @param iOvrl Overlay {@link Image} instance(s) - if <code>null</code>
     * then original instance is returned
     * @param p Position of overlay image - specified as a {@link BorderLayoutPosition}
     * @param wPct Percentage of the original image width to overlay - ignored
     * if non-positive
     * @param hPct Percentage of the original image height to overlay - ignored
     * if non-positive
     * @param o The {@link ImageObserver} instance to use
     * @return A best effort {@link Image} that overlays the image over
     * the original - may be same as original/overlay if bad/illegal arguments
     */
    public static final Image getOverlayImage (
                final Image                 iOrig,
                final Image                 iOvrl,
                final BorderLayoutPosition    p,
                final int                    wPct,
                final int                    hPct,
                final ImageObserver            o)
    {
        if (null == iOrig)
            return iOvrl;
        else if (null == iOvrl)
            return iOrig;

        if ((null == p) || (wPct <= 0) || (hPct <= 0))
            return iOrig;

        final Integer    h=getHeight(iOrig), w=getWidth(iOrig);
        if ((null == h) || (null == w))
            return iOrig;

        // calculate the relative size of the overlay image
        final int    oWidth=w.intValue(),
                    oHeight=h.intValue(),
                    vWidth=(wPct >= 100) ? oWidth : (oWidth * wPct) / 100,
                    vHeight=(hPct >= 100) ? oHeight : (oHeight * hPct) / 100;

        final Rectangle    r=
            new Rectangle(0, 0, oWidth, oHeight),
                        dp=
            getImageDrawBounds(new Dimension(vWidth, vHeight), r, p);
        if ((null == dp) || (dp.width <= 0) || (dp.height <= 0))
            return iOrig;

        // create a copy of the original since we are going to draw into it
        final Image        ret=
            new BufferedImage(oWidth, oHeight, (iOrig instanceof BufferedImage) ? ((BufferedImage) iOrig).getType() : BufferedImage.TYPE_INT_ARGB);
        final Graphics    g=ret.getGraphics();
        g.drawImage(iOrig, 0, 0, oWidth, oHeight, o);
        g.drawImage(iOvrl, dp.x, dp.y, dp.width, dp.height, o);
        g.dispose();
        return ret;
    }
    /**
     * @param iOrig Original {@link Icon} instance - if <code>null</code>
     * then overlay instance is returned
     * @param iOvrl Overlay {@link Icon} instance(s) - if <code>null</code>
     * then original instance is returned
     * @param p Position of overlay icon - specified as a {@link BorderLayoutPosition}
     * @param wPct Percentage of the original icon width to overlay - ignored
     * if non-positive
     * @param hPct Percentage of the original icon height to overlay - ignored
     * if non-positive
     * @param o The {@link ImageObserver} instance to use
     * @return A best effort {@link Icon} image that overlays the icon over
     * the original - may be same as original/overlay if bad/illegal arguments
     */
    public static final Icon getOverlayIcon (
                final Icon                     iOrig,
                final Icon                     iOvrl,
                final BorderLayoutPosition    p,
                final int                    wPct,
                final int                    hPct,
                final ImageObserver            o)
    {
        if (null == iOrig)
            return iOvrl;
        else if (null == iOvrl)
            return iOrig;

        if ((null == p) || (wPct <= 0) || (hPct <= 0))
            return iOrig;

        final Image    imgOrig=
            (iOrig instanceof ImageIcon) ? ((ImageIcon) iOrig).getImage() : null,
                    imgOvrl=
            (iOvrl instanceof ImageIcon) ? ((ImageIcon) iOvrl).getImage() : null,
                    img=
            getOverlayImage(imgOrig, imgOvrl, p, wPct, hPct, o);

        if (null == img)
            return iOrig;
        else if (img == imgOrig)
            return iOrig;
        else if (img == imgOvrl)
            return iOvrl;
        else
            return new ImageIcon(img);
    }
}
