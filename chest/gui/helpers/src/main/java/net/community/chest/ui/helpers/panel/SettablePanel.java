/*
 *
 */
package net.community.chest.ui.helpers.panel;

import java.awt.LayoutManager;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.community.chest.ui.helpers.HelperUtils;
import net.community.chest.ui.helpers.SettableComponent;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> Type of value expected
 * @author Lyor G.
 * @since Jan 1, 2009 12:31:02 PM
 */
public class SettablePanel<V> extends HelperPanel implements SettableComponent<V> {
    /**
     *
     */
    private static final long serialVersionUID = -2465856913079193975L;
    public SettablePanel (boolean isDoubleBuffered, boolean autoInit)
    {
        super(isDoubleBuffered, autoInit);
    }

    public SettablePanel (Element elem, boolean autoInit)
    {
        super(elem, autoInit);
    }

    public SettablePanel (Element elem)
    {
        this(elem, true);
    }


    public SettablePanel (LayoutManager layout, Document doc, boolean autoInit)
    {
        super(layout, doc, autoInit);
    }

    public SettablePanel (Document doc, boolean autoInit)
    {
        this(null, doc, autoInit);
    }

    public SettablePanel (Document doc)
    {
        this(doc, true);
    }

    public SettablePanel (LayoutManager layout, boolean autoLayout)
    {
        this(layout, (Document) null, autoLayout);
    }

    public SettablePanel (LayoutManager layout)
    {
        this(layout, true);
    }

    public SettablePanel (boolean autoLayout)
    {
        this((Document) null, autoLayout);
    }

    public SettablePanel ()
    {
        this(true);
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
