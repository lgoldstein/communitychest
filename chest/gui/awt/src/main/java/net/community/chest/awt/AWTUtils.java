/*
 *
 */
package net.community.chest.awt;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 30, 2008 4:20:29 PM
 */
public final class AWTUtils {
    private AWTUtils ()
    {
        // no instance
    }

    private static final AtomicInteger    _eventIdsAssignment=new AtomicInteger(AWTEvent.RESERVED_ID_MAX);
    /**
     * @return A unique valid {@link AWTEvent} ID that can be used by user-defined events
     * @throws IllegalStateException If ran out of ID(s)
     * @see AWTEvent#RESERVED_ID_MAX
     */
    public static final int assignUserEventID () throws IllegalStateException
    {
        final int    eventId=_eventIdsAssignment.incrementAndGet();
        if (eventId <= AWTEvent.RESERVED_ID_MAX)
            throw new IllegalStateException("Bad AWTEvent ID value: " + eventId);
        return eventId;
    }

    private static final int getAdjustedDimensionValue (final int v, final int minVal, final int maxVal)
    {
        if ((maxVal > 0) && (v > maxVal))
            return maxVal;
        if ((minVal > 0) && (v < minVal))
            return minVal;

        return v;
    }

    public static final Dimension checkDimensions (final int w, final int minWidth, final int maxWidth,
                                                   final int h, final int minHeight, final int maxHeight)
    {
        final int    ah=getAdjustedDimensionValue(h, minHeight, maxHeight),
                    aw=getAdjustedDimensionValue(w, minWidth, maxWidth);
        if ((h != ah) || (w != aw))
            return new Dimension(aw, ah);

        return null;
    }
    /**
     * Checks if the current {@link Component} width/height are within
     * specified limits
     * @param c The {@link Component} to be checked - ignored if <code>null</code>
     * @param minWidth Minimum width (pixels) - if non-positive, then no limit
     * @param maxWidth Maximum width (pixels) - if non-positive, then no limit
     * @param minHeight Minimum height (pixels) - if non-positive, then no limit
     * @param maxHeight Maximum height (pixels) - if non-positive, then no limit
     * @return A {@link Dimension} containing the allowed dimensions -
     * <code>null</code> if current dimensions are within limits (or no component)
     */
    public static final Dimension checkDimensions (final Component c,
            final int minWidth, final int maxWidth,
            final int minHeight, final int maxHeight)
    {
        if (null == c)
            return null;

        return checkDimensions(c.getWidth(), minWidth, maxWidth, c.getHeight(), minHeight, maxHeight);
    }

    public static final void setOpaque (Component comp, boolean opaque)
    {
        if (comp instanceof JComponent)
            ((JComponent) comp).setOpaque(opaque);

        final Collection<? extends Component>    ca;
        if (comp instanceof JScrollPane)
        {
            final JViewport viewport=((JScrollPane) comp).getViewport();
            ca = Arrays.asList(viewport, (null == viewport) ? null : viewport.getView());
        }
        else if (comp instanceof JSplitPane)
        {
            final JSplitPane    sp=(JSplitPane) comp;
            ca = Arrays.asList(sp.getLeftComponent(), sp.getRightComponent());
        }
        else if (comp instanceof Container)
        {
            final Component[]    cl=((Container) comp).getComponents();
            ca = ((null == cl) || (cl.length <= 0)) ? null : Arrays.asList(cl);
        }
        else
            ca = null;

        if ((ca != null) && (ca.size() > 0))
        {
            for (final Component c : ca)
                setOpaque(c, opaque);
        }
    }
    /**
     * @param d Total available display area {@link Dimension}
     * @param margin If not <code>null</code> then the "reserved" offsets
     * from the available display area that should not be used
     * @return A {@link Rectangle} denoting the available display area out
     * of the total after taking into account the margin (if any) - may be
     * "empty" (i.e. all zeroes) if no dimension given
     */
    public static final Rectangle getDisplayArea (final Dimension d, final Insets margin)
    {
        if (null == d)
            return new Rectangle();

        final Rectangle    r=new Rectangle(0, 0, Math.max(d.width,0), Math.max(d.height, 0));
        if (margin != null)
        {
            r.x = Math.max(margin.left, 0);
            r.width = Math.max(0, r.width - (r.x + Math.max(0, margin.right)));

            r.y = Math.max(margin.top, 0);
            r.height = Math.max(0, r.height - (r.y + Math.max(margin.bottom, 0)));
        }

        return r;
    }
    /**
     * @param c The {@link Component} whose {@link Component#getSize()} is
     * used to retrieve the total available display area
     * @param margin If not <code>null</code> then the "reserved" offsets
     * from the available display area that should not be used
     * @return A {@link Rectangle} denoting the available display area out
     * of the total after taking into account the margin (if any) - may be
     * "empty" (i.e. all zeroes) if no dimension given
     */
    public static final Rectangle getDisplayArea (Component c, Insets margin)
    {
        return getDisplayArea((null == c) ? null : c.getSize(), margin);
    }
    /**
     * Locates a {@link Component} in a hierarchy by querying its {@link Component#getName()}
     * @param c The root component to start looking for (inclusive). If not
     * matches the name and it is a {@link Container} then it is scanned
     * <U>recursively</U>. If <code>null</code> then same as if no match.
     * @param name Name to look for - if <code>null</code>/empty then as if
     * no match
     * @param caseSensitive Comparison case sensitivity
     * @return The <U>first</U> match found - <code>null</code> if no match found
     */
    public static final Component findComponentByName (Component c, String name, boolean caseSensitive)
    {
        if ((null == c) || (null == name) || (name.length() <= 0))
            return null;

        final String    n=c.getName();
        if (0 == StringUtil.compareDataStrings(n, name, caseSensitive))
            return c;

        if (c instanceof Container)
        {
            final Component[]    ca=((Container) c).getComponents();
            if ((null == ca) || (ca.length <= 0))
                return null;    // debug breakpoint

            for (final Component cc : ca)
            {
                final Component    sc=findComponentByName(cc, name, caseSensitive);
                if (sc != null)
                    return sc;
            }
        }

        return null;
    }

    public static final Map<String,Component> updateComponentsMap (final Map<String,Component> org, final Component c)
    {
        if (null == c)
            return org;

        final String            n=c.getName();
        Map<String,Component>    ret=org;
        if ((n != null) && (n.length() > 0))
        {
            if (null == ret)
                ret = new TreeMap<String,Component>(String.CASE_INSENSITIVE_ORDER);
            ret.put(n, c);
        }

        if (c instanceof Container)
        {
            final Component[]    ca=((Container) c).getComponents();
            if ((null == ca) || (ca.length <= 0))
                return ret;    // debug breakpoint

            for (final Component cc : ca)
                ret = updateComponentsMap(ret, cc);
        }

        return ret;
    }
    /**
     * @param c The root component to start looking for (inclusive)
     * @return <P>A {@link Map} of all {@link Component}-s in the root one's
     * hierarchy where key={@link Component#getName()}, value=the {@link Component}.
     * <B>Note(s)</B>:</P></BR>
     * <UL>
     *         <LI>if no component or no name, then nothing is mapped</LI>
     *         <LI>if same name used again, then the entry will be overwritten</LI>
     * <UL>
     */
    public static final Map<String,Component> getComponentsMap (final Component c)
    {
        return updateComponentsMap(null, c);
    }
    /**
     * An array of all {@link InputEvent} modifiers masks
     * @see #isKeyModifiedInputEventMask(int)
     */
    private static final int[]    INPUT_MODIFIERS={
            InputEvent.ALT_MASK,
            InputEvent.ALT_GRAPH_MASK,
            InputEvent.CTRL_MASK,
            InputEvent.META_MASK,
            InputEvent.SHIFT_MASK
        };
    /**
     * @param m An {@link InputEvent#getModifiers()} mask result
     * @return TRUE if the mask contains any of the {@link #INPUT_MODIFIERS}
     */
    public static final boolean isKeyModifiedInputEventMask (final int m)
    {
        for (final int v : INPUT_MODIFIERS)
        {
            if ((v & m) != 0)
                return true;
        }

        return false;
    }
    /**
     * @param e An {@link InputEvent}
     * @return TRUE if non-null event and any of the extra modification keys
     * has been pressed (e.g., CTRL, ALT, etc.)
     * @see InputEvent#isAltDown()
     * @see InputEvent#isAltGraphDown()
     * @see InputEvent#isControlDown()
     * @see InputEvent#isMetaDown()
     * @see InputEvent#isShiftDown()
     */
    public static final boolean isKeyModifiedInputEvent (final InputEvent e)
    {
        if (null == e)
            return false;

        if (e.isAltDown()
         || e.isAltGraphDown()
         || e.isControlDown()
         || e.isMetaDown()
         || e.isShiftDown())
            return true;

        return false;
    }
    /**
     * Adds all non-<code>null</code> components to the specified container
     * @param container The {@link Container} to add the components to - ignored if <code>null</code>
     * @param comps The {@link Collection} of {@link Component}s to be added - ignored if <code>null</code>/empty
     * @return The actual number of non-<code>null</code> components added to the specified container
     */
    public static final int addComponents (Container container, Collection<? extends Component> comps)
    {
        if ((container == null) || (comps == null) || (comps.size() <= 0))
            return 0;

        int    numAdded=0;
        for (final Component c : comps)
        {
            if (c == null)
                continue;
            container.add(c);
            numAdded++;
        }

        return numAdded;
    }
    /**
     * Adds all non-<code>null</code> components to the specified container
     * @param container The {@link Container} to add the components to - ignored if <code>null</code>
     * @param comps The {@link Component}s to be added
     * @return The actual number of non-<code>null</code> components added to the specified container
     */
    public static final int addComponents (Container container, Component ... comps)
    {
        if ((container == null) || (comps == null) || (comps.length <= 0))
            return 0;

        return addComponents(container, Arrays.asList(comps));
    }
    /**
     * Adds a {@link KeyListener} to all the specified {@link Component}-s
     * @param l The {@link KeyListener} to add - ignored if <code>null</code>
     * @param comps The {@link Component}-s to add the listener to - ignored
     * if <code>null</code>/empty
     * @return Number of components to which the listener was added
     */
    public static final int addKeyListener (final KeyListener l, final Collection<? extends Component> comps)
    {
        if ((l == null) || (comps == null) || (comps.size() <= 0))
            return 0;

        int    numAdded=0;
        for (final Component c : comps)
        {
            if (c == null)
                continue;
            c.addKeyListener(l);
            numAdded++;
        }

        return numAdded;

    }
    /**
     * Adds a {@link KeyListener} to all the specified {@link Component}-s
     * @param l The {@link KeyListener} to add - ignored if <code>null</code>
     * @param comps The {@link Component}-s to add the listener to - ignored
     * if <code>null</code>/empty
     * @return Number of components to which the listener was added
     */
    public static final int addKeyListener (final KeyListener l, final Component ... comps)
    {
        if ((l == null) || (comps == null) || (comps.length <= 0))
            return 0;

        return addKeyListener(l, Arrays.asList(comps));
    }
}
