/*
 *
 */
package net.community.chest.ui.helpers.window;

import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Disposes the {@link Window} if any of the defined keys is pressed</P>
 *
 * @author Lyor G.
 * @since Oct 14, 2009 3:30:49 PM
 */
public class WindowDisposeKeyListener extends KeyAdapter {
    private Window    _w;
    public Window getWindow ()
    {
        return _w;
    }

    public void setWindow (Window w)
    {
        _w = w;
    }

    private int[]    _keys;
    public int[] getDisposeKeys ()
    {
        return _keys;
    }

    public WindowDisposeKeyListener (Window w, int ... keys)
    {
        _w = w;
        _keys = keys;
    }

    public WindowDisposeKeyListener (int ... keys)
    {
        this(null, keys);
    }
    /**
     * Default keys used to indicate window disposal
     */
    public static final int[]    DEFAULT_DISPOSE_KEYS={ KeyEvent.VK_ESCAPE };
    public WindowDisposeKeyListener (Window w)
    {
        this(w, DEFAULT_DISPOSE_KEYS);
    }

    public WindowDisposeKeyListener ()
    {
        this(DEFAULT_DISPOSE_KEYS);
    }
    /*
     * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased (KeyEvent e)
    {
        final int        kc=(null == e) ? Integer.MIN_VALUE : e.getKeyCode();
        final Window    w=getWindow();
        final int[]        ka=(null == w) ? null : getDisposeKeys();
        if ((null == ka) || (ka.length <= 0))
            return;

        for (final int    kv : ka)
        {
            if (kv == kc)
            {
                w.dispose();
                return;
            }
        }
    }
}
