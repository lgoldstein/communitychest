/*
 *
 */
package net.community.chest.ui.helpers.tabbed;

import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.community.chest.swing.component.tabbed.TabLayoutPolicy;
import net.community.chest.swing.component.tabbed.TabPlacement;
import net.community.chest.ui.helpers.HelperUtils;
import net.community.chest.ui.helpers.SettableComponent;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Propagates all its {@link SettableComponent} calls to all the current
 * tabs that also implement {@link SettableComponent} (and only to them)
 * which are <U>all</U> assumed to expect the <U>same</U> value type</P>
 *
 * @param <V> The expected value type for each sub-component/tab
 * @author Lyor G.
 * @since Jan 1, 2009 10:32:50 AM
 */
public class SettableTabbedPane<V> extends HelperTabbedPane implements SettableComponent<V> {
    /**
     *
     */
    private static final long serialVersionUID = -7766258835185387699L;
    public SettableTabbedPane (Element elem, boolean autoInit)
    {
        super(elem, autoInit);
    }

    public SettableTabbedPane (Document doc, boolean autoInit)
    {
        super(doc, autoInit);
    }

    public SettableTabbedPane (boolean autoInit)
    {
        super(autoInit);
    }

    public SettableTabbedPane (int placement, boolean autoInit)
    {
        super(placement, autoInit);
    }

    public SettableTabbedPane (int placement, int layoutPolicy, boolean autoInit)
    {
        super(placement, layoutPolicy, autoInit);
    }

    public SettableTabbedPane (Element elem)
    {
        this(elem, true);
    }

    public SettableTabbedPane (Document doc)
    {
        this(doc, true);
    }

    public SettableTabbedPane ()
    {
        this(true);
    }

    public SettableTabbedPane (int placement)
    {
        this(placement, true);
    }

    public SettableTabbedPane (int placement, int layoutPolicy)
    {
        this(placement, layoutPolicy, true);
    }

    public SettableTabbedPane (TabPlacement p, TabLayoutPolicy l, boolean autoInit)
    {
        super(p, l, autoInit);
    }

    public SettableTabbedPane (TabPlacement p, TabLayoutPolicy l)
    {
        this(p, l, true);
    }

    public SettableTabbedPane (TabPlacement p, boolean autoInit)
    {
        super(p, autoInit);
    }

    public SettableTabbedPane (TabPlacement p)
    {
        this(p, true);
    }
    /**
     * Recursively invokes the {@link SettableComponent} method on all
     * tab components that implement it.
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
