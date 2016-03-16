/*
 *
 */
package net.community.chest.ui.helpers.label;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.Icon;

import net.community.chest.awt.event.ActionListenerSet;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.label.BaseLabel;
import net.community.chest.ui.helpers.XmlElementComponentInitializer;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Based on code from another <A HREF="http://www.devdaily.com/blog/post/jfc-swing/emulate-hyperlink-in-java-with-jlinklabel/">site</A></P>
 *
 * @author Lyor G.
 * @since Nov 12, 2008 12:38:13 PM
 */
public class JHyperlink extends BaseLabel implements XmlElementComponentInitializer {
    /**
     *
     */
    private static final long serialVersionUID = 1674128775962605436L;

    private String _url;
    public String getUrl ()
    {
        return _url;
    }

    public void setUrl (String url)
    {
        _url = url;
    }

        /* Default colors scheme */
    public static final Color    COLOR_FG_NORMAL=Color.BLUE,
                                COLOR_BG_NORMAL=Color.LIGHT_GRAY,
                                COLOR_HOVER=Color.RED,
                                COLOR_FG_ACTIVE=COLOR_FG_NORMAL,
                                COLOR_BG_ACTIVE=Color.LIGHT_GRAY;

    private Color    _fgNormal=COLOR_FG_NORMAL;
    public Color getNormalFgColor ()
    {
        return _fgNormal;
    }

    public void setNormalFgColor (Color c)
    {
        if (c != null)
            _fgNormal = c;
    }

    private Color    _bgNormal=COLOR_BG_NORMAL;
    public Color getNormalBgColor ()
    {
        return _bgNormal;
    }

    public void setNormalBgColor (Color c)
    {
        if (c != null)
            _bgNormal = c;
    }

    private Color    _hoverColor=COLOR_HOVER;
    public Color getHoverColor ()
    {
        return _hoverColor;
    }

    public void setHoverColor (Color c)
    {
        if (c != null)
            _hoverColor = c;
    }

    private Color    _fgActive=COLOR_FG_ACTIVE;
    public Color getActiveFgColor ()
    {
        return _fgActive;
    }

    public void setActiveFgColor (Color c)
    {
        if (c != null)
            _fgActive = c;
    }

    private Color    _bgActive=COLOR_BG_ACTIVE;
    public Color getActiveBgColor ()
    {
        return _bgActive;
    }

    public void setActiveBgColor (Color c)
    {
        if (c != null)
            _bgActive = c;
    }

    private Color    _mouseOutColor=COLOR_FG_NORMAL;
    protected Color getMouseOutColor ()
    {
        return _mouseOutColor;
    }

    protected void setMouseOutColor (Color c)
    {
        if (c != null)
            _mouseOutColor = c;
    }
    /**
     * Type of {@link Cursor} to use if {@link #setCursor(Cursor)} no called
     * with a non-null value
     */
    private int    _hoverCursorType=Cursor.HAND_CURSOR;
    public int getHoverCursorType ()
    {
        return _hoverCursorType;
    }

    public void setHoverCursorType (int type)
    {
        _hoverCursorType = type;
    }

    private Cursor    _hoverCursor    /* =null */;
    public Cursor getHoverCursor ()
    {
        return _hoverCursor;
    }

    public void setHoverCursor (Cursor c)
    {
        _hoverCursor = c;
    }

    protected Cursor resolveHoverCursor ()
    {
        Cursor    c=getHoverCursor();
        if (null == c)
        {
            final int    t=getHoverCursorType();
            c = Cursor.getPredefinedCursor(t);
        }

        return c;
    }

    private Collection<ActionListener>    _al;
    public boolean addActionListener (ActionListener l)
    {
        if (null == l)
            return false;

        synchronized(this)
        {
            if (null == _al)
                _al = new ActionListenerSet();
        }

        synchronized(_al)
        {
            return _al.add(l);
        }
    }

    protected int fireActionListeners (final Collection<? extends ActionListener> al)
    {
        final int    numListeners=(null == al) ? 0 : al.size();
        if (numListeners <= 0)
            return 0;

        final ActionEvent    event=new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getUrl());
        for (final ActionListener l : al)
        {
            if (null == l)
                continue;

            l.actionPerformed(event);
        }

        return numListeners;
    }

    protected int fireActionListeners ()
    {
        // use a copy in order to avoid concurrent modifications
        final Collection<? extends ActionListener>    al;
        synchronized(this)
        {
            if ((null == _al) || (_al.size() <= 0))
                return 0;

            al = new LinkedList<ActionListener>(_al);
        }

        return fireActionListeners(al);
    }
    /**
     * Called by the {@link MouseListener} when user clicked on the URL in
     * order to activate it. The default calls {@link #fireActionListeners()}
     * method
     */
    public void activateURL ()
    {
        fireActionListeners();
    }

    protected class HyperlinkMouseListener implements MouseListener {
        public HyperlinkMouseListener ()
        {
            super();
        }
        /* Note: the exception from the call to "activateURL" is embedded
         * into a RuntimeException whose cause is the original exception
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseClicked (MouseEvent me) throws RuntimeException
        {
            final Color    c=getActiveFgColor();
            setForeground(c);
            setMouseOutColor(c);

            try
            {
                activateURL();
            }
            catch(Exception e)
            {
                throw ExceptionUtil.toRuntimeException(e);
            }
        }
        /*
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseReleased (MouseEvent me)
        {
            // do nothing
        }
        /*
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        @Override
        public void mousePressed (MouseEvent me)
        {
            setMouseOutColor(getActiveFgColor());
        }
        /*
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseEntered (MouseEvent me)
        {
            setForeground(getHoverColor());
            setBackground(getActiveBgColor());
            setCursor(resolveHoverCursor());
        }
        /*
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseExited (MouseEvent me)
        {
            setForeground(getMouseOutColor());
            setBackground(getNormalBgColor());
            setCursor(Cursor.getDefaultCursor());
        }
    }

    protected MouseListener initMouseListener ()
    {
        return new HyperlinkMouseListener();
    }

    private Element    _elem;
    /*
     * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#getComponentElement()
     */
    @Override
    public Element getComponentElement () throws RuntimeException
    {
        return _elem;
    }
    /*
     * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#setComponentElement(org.w3c.dom.Element)
     */
    @Override
    public void setComponentElement (Element elem)
    {
        if (_elem != elem)
            _elem = elem;
    }
    /*
     * @see net.community.chest.swing.component.label.BaseLabel#getLabelConverter(org.w3c.dom.Element)
     */
    @Override
    public XmlProxyConvertible<?> getLabelConverter (Element elem)
    {
        return (null == elem) ? null : JHyperlinkReflectiveProxy.HYPERLINK;
    }
    /*
     * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#layoutComponent(org.w3c.dom.Element)
     */
    @Override
    public void layoutComponent (Element elem) throws RuntimeException
    {
        if (elem != null)
        {
            try
            {
                if (fromXml(elem) != this)
                    throw new IllegalStateException("layoutComponent(" + DOMUtils.toString(elem) + ") mismatched re-constructed instance");
            }
            catch(Exception e)
            {
                throw ExceptionUtil.toRuntimeException(e);
            }

            setComponentElement(elem);
        }

        final MouseListener    l=initMouseListener();
        if (l != null)
            addMouseListener(l);

        setForeground(getNormalFgColor());
        setBackground(getNormalBgColor());

        setSize(getPreferredSize());
        setOpaque(true);
    }
    /*
     * @see net.community.chest.ui.helpers.ComponentInitializer#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        layoutComponent(getComponentElement());
    }

    public JHyperlink (String text, Icon icon, int horizontalAlignment, String url)
    {
        super(text, icon, horizontalAlignment);
        _url = url;
        layoutComponent();
    }

    public JHyperlink (String text, Icon icon, int horizontalAlignment)
    {
        this(text, icon, horizontalAlignment, null);
    }

    public JHyperlink (Icon image)
    {
        this(null, image, CENTER);
    }

    public JHyperlink (Icon image, String url)
    {
        this(null, image, CENTER, url);
    }

    public JHyperlink (String text, int horizontalAlignment)
    {
        this(text, null, horizontalAlignment);
    }

    public JHyperlink (String text, int horizontalAlignment, String url)
    {
        this(text, null, horizontalAlignment, url);
    }

    public JHyperlink (Icon image, int horizontalAlignment)
    {
        this(null, image, horizontalAlignment);
    }

    public JHyperlink (Icon image, int horizontalAlignment, String url)
    {
        this(null, image, horizontalAlignment, url);
    }

    public JHyperlink (String text, String url)
    {
        this(text, null, LEADING, url);
    }

    public JHyperlink (String text)
    {
        this(text, null);
    }

    public JHyperlink ()
    {
        this("");
    }
    /*
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint (final Graphics g)
    {
        super.paint(g);

        final Dimension    d=getPreferredSize();
        final int        h=getHeight();
        g.drawLine(2, h-1, (int)d.getWidth()-2, h-1);
    }
}
