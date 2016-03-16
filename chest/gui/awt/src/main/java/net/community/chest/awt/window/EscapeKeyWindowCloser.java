/*
 *
 */
package net.community.chest.awt.window;

import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import net.community.chest.awt.AWTUtils;

/**
 * Interprets the ESC(ape) key as a request to close the {@link Window} it tracks
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 2, 2010 10:14:56 AM
 */
public class EscapeKeyWindowCloser extends KeyAdapter {
    /**
     * TRUE if window has already been closed once
     */
    private boolean    _closed;
    public boolean isClosed ()
    {
        return _closed;
    }

    public void setClosed (boolean closed)
    {
        _closed = closed;
    }
    /**
     * The tracked {@link Window}
     */
    private Window    _w;
    public Window getWindow ()
    {
        return _w;
    }

    public void setWindow (Window w)
    {
        _w = w;
    }

    public void setWindow (Window w, boolean closed)
    {
        _w = w;
        _closed = closed;
    }

    public EscapeKeyWindowCloser (Window w, boolean closed)
    {
        _w = w;
        _closed = closed;
    }

    public EscapeKeyWindowCloser (Window w)
    {
        this(w, false);
    }

    public EscapeKeyWindowCloser ()
    {
        this(null);
    }
    /**
     * Closes the tracked {@link Window} if available and not already closed.
     * @return TRUE if called {@link Window#dispose()}. If so, the it also calls
     * {@link #setClosed(boolean)} with TRUE to avoid re-closing the window.
     */
    public boolean closeWindow ()
    {
        // avoid repeated calls to "dispose" if already closed
        if (isClosed())
            return false;

        final Window    w=getWindow();
        if (null == w)
            return false;

        w.dispose();
        setClosed(true);
        return true;
    }
    /*
     * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped (KeyEvent e)
    {
        if (null == e)
            return;

        if (AWTUtils.isKeyModifiedInputEvent(e))
            return;

        final int    kc=e.getKeyChar();
        if (kc != KeyEvent.VK_ESCAPE)
            return;

        if (!closeWindow())
            return;    // debug breakpoint
    }
}
