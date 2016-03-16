/*
 *
 */
package net.community.chest.swing.component.menu;

import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JMenuItem;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <I> Type of {@link JMenuItem} being mapped
 * @author Lyor G.
 * @since Aug 27, 2008 1:18:41 PM
 */
public class MenuItemsMap<I extends JMenuItem> extends TreeMap<String,I> implements MenuItemExplorer {
    /**
     *
     */
    private static final long serialVersionUID = -6894255618145011614L;
    public MenuItemsMap (Comparator<? super String> comparator)
    {
        super(comparator);
    }
    // NOTE: default is case INSENSITIVE
    public MenuItemsMap ()
    {
        this(String.CASE_INSENSITIVE_ORDER);
    }

    public MenuItemsMap (Map<? extends String,? extends I> m)
    {
        this(String.CASE_INSENSITIVE_ORDER);
        putAll(m);
    }

    public MenuItemsMap (SortedMap<String,? extends I> m)
    {
        super(m);
    }
    /*
     * @see net.community.chest.swing.component.menu.MenuItemExplorer#getItemsMap()
     */
    @Override
    public Map<String,? extends JMenuItem> getItemsMap ()
    {
        return this;
    }
    /*
     * @see net.community.chest.swing.component.menu.MenuItemExplorer#resetItemsMap()
     */
    @Override
    public void resetItemsMap ()
    {
        clear();
    }
    /*
     * @see net.community.chest.swing.component.menu.MenuItemExplorer#findMenuItemByCommand(java.lang.String)
     */
    @Override
    public I findMenuItemByCommand (final String cmd)
    {
        if ((null == cmd) || (cmd.length() <= 0))
            return null;

        return get(cmd);
    }
    /*
     * @see net.community.chest.swing.component.menu.MenuItemExplorer#addItemActionListenerByCommand(java.lang.String, java.awt.event.ActionListener)
     */
    @Override
    public I addItemActionListenerByCommand (String cmd, ActionListener l)
    {
        final I    item=findMenuItemByCommand(cmd);
        if ((item != null) && (l != null))
            item.addActionListener(l);

        return item;
    }
}
