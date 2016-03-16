/*
 *
 */
package net.community.chest.ui.helpers.button;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;

import javax.swing.Icon;

import net.community.chest.lang.ExceptionUtil;

import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Calls {@link Window#dispose()} <U>after</U> calling {@link Callable#call()}
 * and getting a non-<code>null</code> result (if any provided)</P>
 *
 * @author Lyor G.
 * @since Apr 2, 2009 3:02:04 PM
 */
public class WindowDisposeButton extends HelperButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 6431963536096438235L;
    private Window    _w;
    public Window getManagedWindow ()
    {
        return _w;
    }

    public void setManagedWindow (Window w)
    {
        _w = w;
    }

    private Callable<?>    _c;
    public Callable<?> getPreDisposeCallable ()
    {
        return _c;
    }

    public void setPreDisposeCallable (Callable<?> c)
    {
        _c = c;
    }
    /*
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed (ActionEvent event)
    {
        final Object    src=(null == event) ? null : event.getSource();
        if (src != this)
            return;

        final Callable<?>    c=getPreDisposeCallable();
        if (c != null)
        {
            try
            {
                final Object    r=c.call();
                if (r == null)
                    return;
            }
            catch(Exception e)
            {
                throw ExceptionUtil.toRuntimeException(e);
            }
        }

        final Window    w=getManagedWindow();
        if (w != null)
            w.dispose();
    }

    protected ActionListener createDisposeActionListener ()
    {
        return this;
    }

    public WindowDisposeButton (Window w, Callable<?> c, String text, Icon icon, Element elem, boolean autoLayout)
    {
        super(text, icon);

        setComponentElement(elem);
        if (autoLayout)
            layoutComponent();

        _w = w;
        _c = c;

        final ActionListener    l=createDisposeActionListener();
        if (l != null)
            addActionListener(l);
    }

    public WindowDisposeButton (Window w, Callable<?> c, String text, Icon icon, Element elem)
    {
        this(w, c, text, icon, elem, true);
    }

    public WindowDisposeButton (Window w, Callable<?> c, String text, Icon icon, boolean autoLayout)
    {
        this(w, c, text, icon, null, autoLayout);
    }

    public WindowDisposeButton (Window w, Callable<?> c, String text, Icon icon)
    {
        this(w, c, text, icon, true);
    }

    public WindowDisposeButton (Window w, Callable<?> c, String text, boolean autoLayout)
    {
        this(w, c, text, null, autoLayout);
    }

    public WindowDisposeButton (Window w, Callable<?> c, String text)
    {
        this(w, c, text, true);
    }

    public WindowDisposeButton (Window w, Callable<?> c, Element elem, boolean autoLayout)
    {
        this(w, c, null, null, elem, autoLayout);
    }

    public WindowDisposeButton (Window w, Callable<?> c, Element elem)
    {
        this(w, c, elem, true);
    }

    public WindowDisposeButton (Window w, Callable<?> c, boolean autoLayout)
    {
        this(w, c, (Element) null, autoLayout);
    }

    public WindowDisposeButton (Window w, Callable<?> c)
    {
        this(w, c, true);
    }

    public WindowDisposeButton ()
    {
        this(null, null);
    }
}
