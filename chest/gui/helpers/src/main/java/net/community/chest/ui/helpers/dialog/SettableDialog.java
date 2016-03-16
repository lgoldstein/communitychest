/*
 *
 */
package net.community.chest.ui.helpers.dialog;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.util.Collection;

import net.community.chest.ui.helpers.HelperUtils;
import net.community.chest.ui.helpers.SettableComponent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> Type of value expected
 * @author Lyor G.
 * @since Jan 1, 2009 12:35:25 PM
 */
public class SettableDialog<V> extends FormDialog implements SettableComponent<V> {
    /**
     *
     */
    private static final long serialVersionUID = -182688181416793225L;
    public SettableDialog (boolean autoInit)
    {
        super(autoInit);
    }

    public SettableDialog (Frame owner, boolean modal, boolean autoInit)
    {
        super(owner, modal, autoInit);
    }

    public SettableDialog (Dialog owner, boolean modal, boolean autoInit)
    {
        super(owner, modal, autoInit);
    }

    public SettableDialog (Frame owner, Element elem, boolean autoInit)
    {
        super(owner, elem, autoInit);
    }

    public SettableDialog (Element elem, boolean autoInit)
    {
        super(elem, autoInit);
    }

    public SettableDialog (Dialog owner, Element elem, boolean autoInit)
    {
        super(owner, elem, autoInit);
    }

    public SettableDialog (Document doc, boolean autoInit)
    {
        super(doc, autoInit);
    }

    public SettableDialog (Frame owner, Document doc, boolean autoInit)
    {
        super(owner, doc, autoInit);
    }

    public SettableDialog (Dialog owner, Document doc, boolean autoInit)
    {
        super(owner, doc, autoInit);
    }

    public SettableDialog (Frame owner, String title, boolean modal, boolean autoInit)
    {
        super(owner, title, modal, autoInit);
    }

    public SettableDialog (Dialog owner, String title, boolean modal, boolean autoInit)
    {
        super(owner, title, modal, autoInit);
    }

    public SettableDialog (Frame owner, String title, boolean modal, GraphicsConfiguration gc, boolean autoInit)
    {
        super(owner, title, modal, gc, autoInit);
    }

    public SettableDialog (Dialog owner, String title, boolean modal, GraphicsConfiguration gc, boolean autoInit)
    {
        super(owner, title, modal, gc, autoInit);
    }

    public SettableDialog ()
    {
        this(true);
    }

    public SettableDialog (Frame owner)
    {
        this(owner, false);
    }

    public SettableDialog (Dialog owner)
    {
        this(owner, false);
    }

    public SettableDialog (Frame owner, boolean modal)
    {
        this(owner, modal, true);
    }

    public SettableDialog (Frame owner, String title)
    {
        this(owner, title, false);
    }

    public SettableDialog (Dialog owner, boolean modal)
    {
        this(owner, modal, true);
    }

    public SettableDialog (Dialog owner, String title)
    {
        this(owner, title, false);
    }

    public SettableDialog (Frame owner, Element elem)
    {
        this(owner, elem, true);
    }

    public SettableDialog (Element elem)
    {
        this(elem, true);
    }

    public SettableDialog (Dialog owner, Element elem)
    {
        this(owner, elem, true);
    }

    public SettableDialog (Document doc)
    {
        this(doc, true);
    }

    public SettableDialog (Frame owner, Document doc)
    {
        this(owner, doc, true);
    }

    public SettableDialog (Dialog owner, Document doc)
    {
        this(owner, doc, true);
    }

    public SettableDialog (Frame owner, String title, boolean modal)
    {
        this(owner, title, modal, true);
    }

    public SettableDialog (Dialog owner, String title, boolean modal)
    {
        this(owner, title, modal, true);
    }

    public SettableDialog (Frame owner, String title, boolean modal, GraphicsConfiguration gc)
    {
        super(owner, title, modal, gc, true);
    }

    public SettableDialog (Dialog owner, String title, boolean modal, GraphicsConfiguration gc)
    {
        this(owner, title, modal, gc, true);
    }

    public SettableDialog (Window owner, Element elem)
    {
        this(owner, elem, true);
    }

    public SettableDialog (Window owner, boolean autoInit)
    {
        super(owner, autoInit);
    }

    public SettableDialog (Window owner, Element elem, boolean autoInit)
    {
        super(owner, elem, autoInit);
    }

    public SettableDialog (Window owner, Document doc, boolean autoInit)
    {
        super(owner, doc, autoInit);
    }

    public SettableDialog (Window owner, ModalityType modalityType, boolean autoInit)
    {
        super(owner, modalityType, autoInit);
    }

    public SettableDialog (Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc, boolean autoInit)
    {
        super(owner, title, modalityType, gc, autoInit);
    }

    public SettableDialog (Window owner, String title, ModalityType modalityType, boolean autoInit)
    {
        super(owner, title, modalityType, autoInit);
    }

    public SettableDialog (Window owner, String title, boolean autoInit)
    {
        super(owner, title, autoInit);
    }
    public SettableDialog (Window owner, Document doc)
    {
        this(owner, doc, true);
    }

    public SettableDialog (Window owner, String title, ModalityType modalityType)
    {
        this(owner, title, modalityType, true);
    }

    public SettableDialog (Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc)
    {
        this(owner, title, modalityType, gc, true);
    }

    public SettableDialog (Window owner, ModalityType modalityType)
    {
        this(owner, modalityType, true);
    }

    public SettableDialog (Window owner, String title)
    {
        this(owner, title, true);
    }

    public SettableDialog (Window owner)
    {
        this(owner, true);
    }

    /**
     * Recursively invokes the {@link SettableComponent} method on all
     * contained components that implement it.
     * @param value The value to use if need to call {@link SettableComponent#setContent(Object)}
     * or {@link SettableComponent#refreshContent(Object)}
     * @param itemNewState <P>The call to invoke on {@link SettableComponent} interface:</P></BR>
     * <UL>
     *         <LI><code>null</code> - invoke {@link SettableComponent#clearContent()}</LI>
     *         <LI><code>TRUE</code> - invoke {@link SettableComponent#setContent(Object)}</LI>
     *         <LI><code>FALSE</code> - invoke {@link SettableComponent#refreshContent(Object)}</LI>
     * </UL>
     * @return A {@link Collection} of all the {@link SettableComponent}
     * on which the interface method was invoked - may be null/empty if no
     * original components to start with or none was a {@link SettableComponent}
     */
    public Collection<SettableComponent<V>> updateSettableComponents (V value, Boolean itemNewState)
    {
        return HelperUtils.updateSettableComponents(this, value, itemNewState);
    }
    /*
     * @see net.community.chest.ui.helpers.SettableComponent#clearContent()
     */
    @Override
    public void clearContent ()
    {
        updateSettableComponents(null, null);
    }
    /*
     * @see net.community.chest.ui.helpers.SettableComponent#refreshContent(java.lang.Object)
     */
    @Override
    public void refreshContent (V value)
    {
        updateSettableComponents(value, Boolean.FALSE);
    }
    /*
     * @see net.community.chest.ui.helpers.SettableComponent#setContent(java.lang.Object)
     */
    @Override
    public void setContent (V value)
    {
        updateSettableComponents(value, Boolean.TRUE);
    }
}
