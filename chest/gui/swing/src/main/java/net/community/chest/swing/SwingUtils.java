/*
 *
 */
package net.community.chest.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JFrame;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Various useful static utilities</P>
 *
 * @author Lyor G.
 * @since Aug 17, 2008 3:44:36 PM
 */
public final class SwingUtils {
    private SwingUtils ()
    {
        // no instance
    }
    /**
     * Finds the container {@link JFrame} in the component tree containing
     * a given {@link Component}.
     * @param c The {@link Component} to search up from - ignored if null
     * @return The containing {@link JFrame} or null if none found
     */
    public static JFrame getContainerFrame (final Component c)
    {
        for (Component cc=c; cc != null; cc = cc.getParent())
        {
            if (cc instanceof JFrame)
                return (JFrame) cc;
        }

        return null;
    }
    /**
     * Sets the location of the given frame in the center of the screen.
     * @param frame The {@link JFrame} to center (ignored if null) -
     * <B>Note:</B> Frame must already have its size set either directly or
     * via <code>pack()</code> call.
     * @return The {@link Point} where the frame top-left corner was set
     * (null if no frame or other problems)
     */
    public static Point center (final JFrame frame)
    {
        if (null == frame)
            return null;

        final Toolkit    defKit=Toolkit.getDefaultToolkit();
        final Dimension screenSize=(null == defKit) ? null : defKit.getScreenSize();
        if (null == screenSize)
            return null;

        final int        w=frame.getWidth(), h=frame.getHeight(),
                        x=Math.max(0,(screenSize.width  - w) / 2),
                        y=Math.max(0,(screenSize.height - h) / 2);
        final Point        p=new Point(x, y);
        frame.setLocation(p);
        return p;
    }
}
