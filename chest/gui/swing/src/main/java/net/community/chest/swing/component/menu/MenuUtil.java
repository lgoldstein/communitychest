package net.community.chest.swing.component.menu;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 24, 2007 3:50:34 PM
 */
public final class MenuUtil {
    private MenuUtil ()
    {
        // no instance
    }
    /**
     * Builds an items {@link Map} that complies with the {@link MenuItemExplorer#getItemsMap()}
     * specification from the supplied {@link JMenu}.
     * @param org original map - if null, one will be created (default=case
     * insensitive {@link TreeMap})
     * @param menu {@link JMenu} instance to be "explored" for items. If one
     * of the sub-items is a menu itself (since {@link JMenu} <code>extends</code>
     * {@link JMenuItem}) then it is "explored" recursively
     * @param errorIfDuplicate TRUE if to <code>throw</code> an {@link IllegalStateException}
     * if an item with same command string is already mapped.
     * @return updated map - may be null/empty if original map was null/empty
     * and nothing added
     * @throws IllegalStateException if <I>errorIfDuplicate</I> parameter is
     * TRUE and a duplicate mapping was found
     */
    public static final Map<String,JMenuItem> updateItemsMap (
            final Map<String,JMenuItem> org, final JMenu menu, final boolean errorIfDuplicate)
        throws IllegalStateException
    {
        Map<String,JMenuItem>    ret=org;
        final int                numItems=(null == menu) ? 0 : menu.getItemCount();
        for (int    i=0; i < numItems; i++)
        {
            final JMenuItem    item=menu.getItem(i);
            if (null == item) // should not happen
                continue;

            // we are interested only in JMenuItem(s) - so explore recursively
            if (item instanceof JMenu)
            {
                ret = updateItemsMap(ret, (JMenu) item, errorIfDuplicate);
                continue;
            }

            final String    cmd=item.getActionCommand();
            if ((null == cmd) || (cmd.length() <= 0))    // TODO check what happens with separator(s)
                continue;

            if (ret != null)
            {
                if (errorIfDuplicate)
                {
                    final JMenuItem    prev=ret.get(cmd);
                    if (prev != null)
                        throw new IllegalStateException("updateItemsMap(" + JMenu.class.getSimpleName() + "=" + menu.getName() + ") duplicate action command: " + cmd);
                }
            }
            else
                ret = new MenuItemsMap<JMenuItem>();
            ret.put(cmd, item);
        }

        return ret;
    }

    public static final Map<String,JMenuItem> getItemsMap (final JMenu menu, final boolean errorIfDuplicate)
    {
        return updateItemsMap(null, menu, errorIfDuplicate);
    }

    public static final Map<String,JMenuItem> getItemsMap (final JMenu menu)
    {
        return getItemsMap(menu, true);
    }
    /**
     * Adds an {@link ActionListener} to a specific menu item given its
     * assigned action command
     * @param ie {@link MenuItemExplorer} to be used to locate the menu item
     * @param cmd command to be used to locate the menu item - <B>Note:</B>
     * case <U>sensitivity<?U> is up to the locator implementation, though it
     * is highly recommended that location algorithm be case <U>insensitive</U>
     * @param l listener to be added to the menu item once located
     * @return located {@link JMenuItem}
     * @throws NoSuchElementException if menu item not found (or null/empty
     * explorer/name/listener)
     */
    public static final JMenuItem addMenuItemActionHandler (
            final MenuItemExplorer ie, final String cmd, final ActionListener l)
        throws NoSuchElementException
    {
        final JMenuItem    item=(null == ie) ? null : ie.addItemActionListenerByCommand(cmd, l);
        if (null == item)
            throw new NoSuchElementException("addMenuItemActionHandler(" + cmd + ") " + ActionListener.class.getName() + " not added");

        return item;
    }

    public static final Map<String,JMenuItem> updateMenuItemsHandlers (
                    final Map<String,JMenuItem>                    org,
                    final MenuItemExplorer                         ie,
                    final Map<String,? extends ActionListener>    lm,
                    final boolean                                 errIfDuplicate)
            throws IllegalStateException
    {
        final Collection<? extends Map.Entry<String,? extends ActionListener>>    ll=
            ((null == ie) || (null == lm) || (lm.size() <= 0)) ? null : lm.entrySet();
        if ((null == ll) || (ll.size() <= 0))
            return org;

        Map<String,JMenuItem>    mm=org;
        for (final Map.Entry<String,? extends ActionListener> le : ll)
        {
            final String            cmd=(null == le) ? null : le.getKey();
            final ActionListener    ml=(null == le) ? null : le.getValue();
            final JMenuItem            item=ie.addItemActionListenerByCommand(cmd, ml);
            if (null == item)
                continue;

            if (null == mm)
                mm = new TreeMap<String,JMenuItem>(String.CASE_INSENSITIVE_ORDER);

            final JMenuItem    prev=mm.put(cmd, item);
            if ((prev != null) && (prev != item) && errIfDuplicate)
                throw new IllegalStateException("updateMenuItemsHandlers(" + cmd + ") duplicate items found");
        }

        return mm;
    }
    /**
     * @param ie The {@link MenuItemExplorer} instance to use to locate
     * menu items
     * @param lm A {@link Map} where key=menu item action command, value=the
     * {@link ActionListener} to associated with the menu item
     * @param errIfDuplicate <code>true</code>=throw an {@link IllegalStateException}
     * if an action command repeated/refers to same menu item
     * @return A {@link Map} where key=menu item action command, value=the
     * {@link JMenuItem} to which the {@link ActionListener} was added
     * @throws IllegalStateException if duplicate mapping found and duplicates
     * not allowed
     */
    public static final Map<String,JMenuItem> setMenuItemsHandlers (
            final MenuItemExplorer ie, final Map<String,? extends ActionListener> lm, final boolean errIfDuplicate)
        throws IllegalStateException
    {
        return updateMenuItemsHandlers(null, ie, lm, errIfDuplicate);
    }
    /**
     * @param ie The {@link MenuItemExplorer} instance to use to locate
     * menu items
     * @param lm A {@link Map} where key=menu item action command, value=the
     * {@link ActionListener} to associated with the menu item
     * @return A {@link Map} where key=menu item action command, value=the
     * {@link JMenuItem} to which the {@link ActionListener} was added
     * @throws IllegalStateException if duplicate mapping found
     */
    public static final Map<String,JMenuItem> setMenuItemsHandlers (
            final MenuItemExplorer ie, final Map<String,? extends ActionListener> lm)
        throws IllegalStateException
    {
        return setMenuItemsHandlers(ie, lm, true);
    }

    public static final Collection<JMenuItem> addMenuItems (
            final Collection<JMenuItem> org, final JMenu menu, final boolean recursive)
    {
        final int    numItems=(null == menu) ? 0 : menu.getItemCount();
        if (numItems <= 0)
            return org;

        Collection<JMenuItem>    ret=org;
        for (int    i=0; i < numItems; i++)
        {
            final JMenuItem    item=menu.getItem(i);
            if (null == item)
                continue;

            if (item instanceof JMenu)
            {
                if (!recursive)
                    continue;

                ret = addMenuItems(ret, (JMenu) item, true);
            }
            else
            {
                if (null == ret)
                    ret = new LinkedList<JMenuItem>();
                ret.add(item);
            }
        }

        return ret;
    }

    public static final Collection<JMenuItem> getMenuItems (
            final JMenu menu, final boolean recursive)
    {
        return addMenuItems(null, menu, recursive);
    }

    public static final JMenuItem getMenuItemByCommand (
            final JMenu menu, final String cmd, final boolean recursive)
    {
        if ((null == menu) || (null == cmd) || (cmd.length() <= 0))
            return null;

        final Collection<? extends JMenuItem>    il=getMenuItems(menu, recursive);
        if ((null == il) || (il.size() <= 0))
            return null;

        for (final JMenuItem i : il)
        {
            final String    ic=(null == i) ? null : i.getActionCommand();
            if (0 == StringUtil.compareDataStrings(ic, cmd, false))
                return i;
        }

        return null;
    }

    public static final Map<String,JMenu> addMenuByCommand (
            final Map<String,JMenu> org, final JMenu menu, final boolean errorIfDuplicate)
        throws IllegalStateException
    {
        final String    cmd=(null == menu) ? null : menu.getActionCommand();
        if ((null == cmd) || (cmd.length() <= 0))
            return org;

        Map<String,JMenu>    ret=org;
        if (ret != null)
        {
            if (errorIfDuplicate)
            {
                final JMenu    prev=ret.get(cmd);
                if (prev != null)
                    throw new IllegalStateException("addMenuByCommand(" + menu.getName() + ") duplicate action command: " + cmd);
            }
        }
        else
            ret = new TreeMap<String,JMenu>(String.CASE_INSENSITIVE_ORDER);

        ret.put(cmd, menu);
        return ret;
    }
    /**
     * Builds an items {@link Map} that complies with the {@link MenuExplorer#getMenusMap()}
     * specification from the supplied {@link JMenu}.
     * @param org original map - if null, one will be created (default=case
     * insensitive {@link TreeMap})
     * @param menu {@link JMenu} instance to be "explored" for sub-menus.
     * @param errorIfDuplicate TRUE if to <code>throw</code> an {@link IllegalStateException}
     * if a menu with same command string is already mapped.
     * @return updated map - may be null/empty if original map was null/empty
     * and nothing added - <B>Note:</B> the parent menu is <U>not</U> mapped
     * @throws IllegalStateException if <I>errorIfDuplicate</I> parameter is
     * TRUE and a duplicate mapping was found
     */
    public static final Map<String,JMenu> updateMenusMap (
            final Map<String,JMenu> org, final JMenu menu, final boolean errorIfDuplicate)
        throws IllegalStateException
    {
        Map<String,JMenu>    ret=org;
        final int            numItems=(null == menu) ? 0 : menu.getItemCount();
        for (int    i=0; i < numItems; i++)
        {
            // we are interested only in JMenu(s) - so explore recursively
            final JMenuItem    item=menu.getItem(i);
            if (!(item instanceof JMenu))
                continue;

            final JMenu    subMenu=(JMenu) item;
            ret = addMenuByCommand(ret, subMenu, errorIfDuplicate);
            ret = updateMenusMap(ret, (JMenu) item, errorIfDuplicate);
        }

        return ret;
    }

    public static final Collection<JMenuItem> setMenuItemsListener (
            final JMenu menu, final ActionListener l)
    {
        if (null == l)
            return null;

        final Collection<JMenuItem>    ml=getMenuItems(menu, true);
        if ((null == ml) || (ml.size() <= 0))
            return null;

        for (final JMenuItem item : ml)
        {
            if (null == item)
                continue;
            item.addActionListener(l);
        }

        return ml;
    }
    /**
     * Adds an {@link ActionListener} to a specific menu item given its
     * assigned action command
     * @param ie {@link MenuItemExplorer} to be used to locate the menu item
     * @param cmd command to be used to locate the menu item - <B>Note:</B>
     * case <U>sensitivity<?U> is up to the locator implementation, though it
     * is highly recommended that location algorithm be case <U>insensitive</U>
     * @param l listener to be added to the menu item once located
     * @param recursive TRUE=add this action listener to all sub-items
     * of the menu (if found) - useful if same handler is used for all items
     * of the menu
     * @return located {@link JMenu}
     * @throws NoSuchElementException if menu not found (or null/empty
     * explorer/name/listener)
     */
    public static final JMenu addMenuActionHandler (
            final MenuExplorer ie, final String cmd, final ActionListener l, final boolean recursive)
        throws NoSuchElementException
    {
        final JMenu    menu=(null == ie) ? null : ie.findMenuByCommand(cmd);
        if (null == menu)
            throw new NoSuchElementException("addMenuActionHandler(" + cmd + ") " + ActionListener.class.getName() + " no menu instance");

        if (null == l)
            return menu;

        if (recursive)
        {
            final Collection<? extends JMenuItem>    ml=setMenuItemsListener(menu, l);
            final int                                numAdded=(null == ml) ? 0 : ml.size();
            // if none added, fall back to adding the listener to the parent menu
            if (numAdded > 0)
                return menu;
        }

        menu.addActionListener(l);
        return menu;
    }
    /**
     * @param org Original/Current {@link Map} of key=menu action command,
     * value=matching {@link JMenu}
     * @param ie The {@link MenuExplorer} to use
     * @param lm A {@link Map} where key=menu action command, value=the
     * {@link ActionListener} to associated with the menu
     * @param recursive <code>true</code> assign the action listener to the
     * menu <U>items</U> rather than the menu itself
     * @param errIfDuplicate <code>true</code>=throw an {@link IllegalStateException}
     * if an action command repeated/refers to same menu
     * @return A {@link Map} where key=menu action command, value=the
     * {@link JMenu} to which the {@link ActionListener} was added (or to whose
     * items it was added - depending on the <code>recursive</code> parameter)
     * @throws IllegalStateException if duplicate mapping found and duplicates
     * not allowed
     */
    public static final Map<String,JMenu> updateMenuActionHandlers (
            final Map<String,JMenu>                        org,
            final MenuExplorer                             ie,
            final Map<String,? extends ActionListener>    lm,
            final boolean                                 recursive,
            final boolean                                errIfDuplicate)
        throws IllegalStateException
    {
        final Collection<? extends Map.Entry<String,? extends ActionListener>>    cl=
            ((null == ie) || (null == lm) || (lm.size() <= 0)) ? null : lm.entrySet();
        if ((null == cl) || (cl.size() <= 0))
            return org;

        Map<String,JMenu>    ret=org;
        for (final Map.Entry<String,? extends ActionListener> ce : cl)
        {
            final String            cmd=(null == ce) ? null : ce.getKey();
            final ActionListener    l=(null == ce) ? null : ce.getValue();
            final JMenu                menu=
                ((null == l) || (null == cmd) || (cmd.length() <= 0)) ? null : ie.findMenuByCommand(cmd);
            if (null == menu)
                continue;

            if (recursive)
            {
                final Collection<? extends JMenuItem>    ml=setMenuItemsListener(menu, l);
                final int                                numAdded=(null == ml) ? 0 : ml.size();
                // if none added, fall back to adding the listener to the parent menu
                if (numAdded <= 0)
                    menu.addActionListener(l);
            }
            else
                menu.addActionListener(l);

            if (null == ret)
                ret = new TreeMap<String,JMenu>(String.CASE_INSENSITIVE_ORDER);

            final JMenu    prev=ret.put(cmd, menu);
            if ((prev != null) && errIfDuplicate)
                throw new IllegalStateException("updateMenuActionHandlers() duplicate mappings for cmd=" + cmd);
        }

        return ret;
    }
    /**
     * @param ie The {@link MenuExplorer} to use
     * @param lm A {@link Map} where key=menu action command, value=the
     * {@link ActionListener} to associated with the menu
     * @param recursive <code>true</code> assign the action listener to the
     * menu <U>items</U> rather than the menu itself
     * @param errIfDuplicate <code>true</code>=throw an {@link IllegalStateException}
     * if an action command repeated/refers to same menu
     * @return A {@link Map} where key=menu action command, value=the
     * {@link JMenu} to which the {@link ActionListener} was added (or to whose
     * items it was added - depending on the <code>recursive</code> parameter)
     * @throws IllegalStateException if duplicate mapping found and duplicates
     * not allowed
     */
    public static final Map<String,JMenu> setMenuActionHandlers (
            final MenuExplorer ie, final Map<String,? extends ActionListener> lm, final boolean recursive, final boolean errIfDuplicate)
        throws IllegalStateException
    {
        return updateMenuActionHandlers(null, ie, lm, recursive, errIfDuplicate);
    }

    public static final Map<String,JMenu> setMenuActionHandlers (
            final MenuExplorer ie, final Map<String,? extends ActionListener> lm, final boolean recursive)
        throws IllegalStateException
    {
        return setMenuActionHandlers(ie, lm, recursive, true);
    }

    public static final Map<String,JMenu> setMenuActionHandlers (
            final MenuExplorer ie, final Map<String,? extends ActionListener> lm)
        throws IllegalStateException
    {
        return setMenuActionHandlers(ie, lm, true);
    }

    public static final MenuItemExplorer resolveMenuItemExplorer (final JMenu mnu)
    {
        if ((null == mnu) || (mnu instanceof MenuItemExplorer))
            return (MenuItemExplorer) mnu;

        final MenuItemsMap<JMenuItem>    ie=new MenuItemsMap<JMenuItem>();
        updateItemsMap(ie, mnu, true);
        return ie;
    }
    /**
     * @param mnu The {@link JMenu} instance to use to locate menu items
     * @param lm A {@link Map} where key=menu item action command, value=the
     * {@link ActionListener} to associated with the menu item
     * @param errIfDuplicate <code>true</code>=throw an {@link IllegalStateException}
     * if an action command repeated/refers to same menu item
     * @return A {@link Map} where key=menu item action command, value=the
     * {@link JMenuItem} to which the {@link ActionListener} was added
     * @throws IllegalStateException if duplicate mapping found and duplicates
     * not allowed
     */
    public static final Map<String,JMenuItem> setMenuItemsHandlers (
            final JMenu mnu, final Map<String,? extends ActionListener> lm, final boolean errIfDuplicate)
        throws IllegalStateException
    {
        return updateMenuItemsHandlers(null, resolveMenuItemExplorer(mnu), lm, errIfDuplicate);
    }
    /**
     * @param mnu The {@link JMenu} instance to use to locate menu items
     * @param lm A {@link Map} where key=menu item action command, value=the
     * {@link ActionListener} to associated with the menu item
     * @return A {@link Map} where key=menu item action command, value=the
     * {@link JMenuItem} to which the {@link ActionListener} was added
     * @throws IllegalStateException if duplicate mapping found
     */
    public static final Map<String,JMenuItem> setMenuItemsHandlers (
            final JMenu mnu, final Map<String,? extends ActionListener> lm)
        throws IllegalStateException
    {
        return setMenuItemsHandlers(mnu, lm, true);
    }

    public static final MenuExplorer resolveMenuExplorer (final JMenu mnu)
    {
        if ((null == mnu) || (mnu instanceof MenuExplorer))
            return (MenuExplorer) mnu;

        final MenusMap<JMenu>    me=new MenusMap<JMenu>();
        updateMenusMap(me, mnu, true);
        return me;
    }

    public static final Map<String,JMenu> setMenuActionHandlers (
            final JMenu menu, final Map<String,? extends ActionListener> lm, final boolean recursive, final boolean errIfDuplicate)
        throws IllegalStateException
    {
        return setMenuActionHandlers(resolveMenuExplorer(menu), lm, recursive, errIfDuplicate);
    }

    public static final Map<String,JMenu> setMenuActionHandlers (
            final JMenu menu, final Map<String,? extends ActionListener> lm, final boolean recursive)
        throws IllegalStateException
    {
        return setMenuActionHandlers(menu, lm, recursive, true);
    }

    public static final Map<String,JMenu> setMenuActionHandlers (
            final JMenu menu, final Map<String,? extends ActionListener> lm)
        throws IllegalStateException
    {
        return setMenuActionHandlers(menu, lm, true);
    }
    /**
     * Builds an items {@link Map} that complies with the {@link MenuExplorer#getMenusMap()}
     * specification from the supplied {@link JMenuBar}.
     * @param org original map - if null, one will be created (default=case
     * insensitive {@link TreeMap})
     * @param bar {@link JMenuBar} instance to be "explored" for items by
     * exploring each of its menus.
     * @param errorIfDuplicate TRUE if to <code>throw</code> an {@link IllegalStateException}
     * if an item with same command string is already mapped.
     * @return updated map - may be null/empty if original map was null/empty
     * and nothing added
     * @throws IllegalStateException if <I>errorIfDuplicate</I> parameter is
     * TRUE and a duplicate mapping was found
     */
    public static final Map<String,JMenu> updateMenusMap (
            final Map<String,JMenu> org, final JMenuBar bar, final boolean errorIfDuplicate)
        throws IllegalStateException
    {
        Map<String,JMenu>    ret=org;
        final int            numMenus=(null == bar) ? 0 : bar.getMenuCount();
        for (int    mIndex=0; mIndex < numMenus; mIndex++)
        {
            final JMenu        menu=bar.getMenu(mIndex);
            ret = addMenuByCommand(ret, menu, errorIfDuplicate);
            ret = updateMenusMap(ret, menu, errorIfDuplicate);
        }

        return ret;
    }
    /**
     * Builds an items {@link Map} that complies with the {@link MenuItemExplorer#getItemsMap()}
     * specification from the supplied {@link JMenuBar}.
     * @param org original map - if null, one will be created (default=case
     * insensitive {@link TreeMap})
     * @param bar {@link JMenuBar} instance to be "explored" for items by
     * exploring each of its menus.
     * @param errorIfDuplicate TRUE if to <code>throw</code> an {@link IllegalStateException}
     * if an item with same command string is already mapped.
     * @return updated map - may be null/empty if original map was null/empty
     * and nothing added
     * @throws IllegalStateException if <I>errorIfDuplicate</I> parameter is
     * TRUE and a duplicate mapping was found
     */
    public static final Map<String,JMenuItem> updateItemsMap (
            final Map<String,JMenuItem> org, final JMenuBar bar, final boolean errorIfDuplicate)
        throws IllegalStateException
    {
        Map<String,JMenuItem>    ret=org;
        final int                numMenus=(null == bar) ? 0 : bar.getMenuCount();
        for (int    mIndex=0; mIndex < numMenus; mIndex++)
        {
            final JMenu    menu=bar.getMenu(mIndex);
            if (menu != null)    // should not be otherwise
                ret = updateItemsMap(ret, menu, errorIfDuplicate);
        }

        return ret;
    }

    public static final Map<String,JMenuItem> getItemsMap (
            final JMenuBar bar, final boolean errorIfDuplicate)
    {
        return updateItemsMap(null, bar, errorIfDuplicate);
    }

    public static final Map<String,JMenuItem> getItemsMap (final JMenuBar bar)
    {
        return getItemsMap(bar, true);
    }

    public static final MenuItemExplorer resolveMenuItemExplorer (final JMenuBar bar)
    {
        if ((null == bar) || (bar instanceof MenuItemExplorer))
            return (MenuItemExplorer) bar;

        final MenuItemsMap<JMenuItem>    ie=new MenuItemsMap<JMenuItem>();
        updateItemsMap(ie, bar, true);
        return ie;
    }

    public static final MenuExplorer resolveMenuExplorer (final JMenuBar bar)
    {
        if ((null == bar) || (bar instanceof MenuExplorer))
            return (MenuExplorer) bar;

        final MenusMap<JMenu>    me=new MenusMap<JMenu>();
        updateMenusMap(me, bar, true);
        return me;
    }
    /**
     * Builds an items {@link Map} that complies with the {@link MenuItemExplorer#getItemsMap()}
     * specification from the supplied {@link JPopupMenu}.
     * @param org original map - if null, one will be created (default=case
     * insensitive {@link TreeMap})
     * @param menu {@link JPopupMenu} instance to be "explored" for items. If one
     * of the sub-items is a menu itself (since {@link JMenu} <code>extends</code>
     * {@link JMenuItem}) then it is "explored" recursively
     * @param errorIfDuplicate TRUE if to <code>throw</code> an {@link IllegalStateException}
     * if an item with same command string is already mapped.
     * @return updated map - may be null/empty if original map was null/empty
     * and nothing added
     * @throws IllegalStateException if <I>errorIfDuplicate</I> parameter is
     * TRUE and a duplicate mapping was found
     */
    public static final Map<String,JMenuItem> updateItemsMap (
            final Map<String,JMenuItem> org, final JPopupMenu menu, final boolean errorIfDuplicate)
                throws IllegalStateException
    {
        Map<String,JMenuItem>    ret=org;
        final int                numItems=(null == menu) ? 0 : menu.getComponentCount();
        for (int    i=0; i < numItems; i++)
        {
            final Component    c=menu.getComponent(i);
            if (null == c) // should not happen
                continue;

            if (!(c instanceof JMenuItem))
                continue;

            // we are interested only in JMenuItem(s) - so explore recursively
            if (c instanceof JMenu)
            {
                ret = updateItemsMap(ret, (JMenu) c, errorIfDuplicate);
                continue;
            }

            final JMenuItem    item=(JMenuItem) c;
            final String    cmd=item.getActionCommand();
            if ((null == cmd) || (cmd.length() <= 0))    // TODO check what happens with separator(s)
                continue;

            if (ret != null)
            {
                if (errorIfDuplicate)
                {
                    final JMenuItem    prev=ret.get(cmd);
                    if (prev != null)
                        throw new IllegalStateException("updateItemsMap(" + JPopupMenu.class.getSimpleName() + "=" + menu.getName() + ") duplicate action command: " + cmd);
                }
            }
            else
                ret = new MenuItemsMap<JMenuItem>();
            ret.put(cmd, item);
        }

        return ret;
    }

    public static final Map<String,JMenuItem> getItemsMap (final JPopupMenu menu, final boolean errorIfDuplicate)
    {
        return updateItemsMap(null, menu, errorIfDuplicate);
    }

    public static final Map<String,JMenuItem> getItemsMap (final JPopupMenu menu)
    {
        return getItemsMap(menu, true);
    }

    public static final Map<String,JMenu> updateMenusMap (
            final Map<String,JMenu> org, final JPopupMenu menu, final boolean errorIfDuplicate)
        throws IllegalStateException
    {
        Map<String,JMenu>    ret=org;
        final int            numItems=(null == menu) ? 0 : menu.getComponentCount();
        for (int    i=0; i < numItems; i++)
        {
            // we are interested only in JMenu(s) - so explore recursively
            final Component    c=menu.getComponent(i);
            if (!(c instanceof JMenu))
                continue;

            ret = addMenuByCommand(ret, (JMenu) c, errorIfDuplicate);
        }

        return ret;
    }

    public static final MenuItemExplorer resolveMenuItemExplorer (final JPopupMenu mnu)
    {
        if ((null == mnu) || (mnu instanceof MenuItemExplorer))
            return (MenuItemExplorer) mnu;

        final MenuItemsMap<JMenuItem>    ie=new MenuItemsMap<JMenuItem>();
        updateItemsMap(ie, mnu, true);
        return ie;
    }

    public static final MenuExplorer resolveMenuExplorer (final JPopupMenu mnu)
    {
        if ((null == mnu) || (mnu instanceof MenuExplorer))
            return (MenuExplorer) mnu;

        final MenusMap<JMenu>    me=new MenusMap<JMenu>();
        updateMenusMap(me, mnu, true);
        return me;
    }

    public static final Collection<JMenuItem> addMenuItems (
            final Collection<JMenuItem> org, final JPopupMenu menu, final boolean recursive)
    {
        final int    numItems=(null == menu) ? 0 : menu.getComponentCount();
        if (numItems <= 0)
            return org;

        Collection<JMenuItem>    ret=org;
        for (int    i=0; i < numItems; i++)
        {
            final Component    c=menu.getComponent(i);
            if (null == c)
                continue;

            if (c instanceof JMenu)
            {
                if (!recursive)
                    continue;

                ret = addMenuItems(ret, (JMenu) c, true);
            }
            else if (c instanceof JMenuItem)
            {
                if (null == ret)
                    ret = new LinkedList<JMenuItem>();
                ret.add((JMenuItem) c);
            }
        }

        return ret;
    }

    public static final Collection<JMenuItem> getMenuItems (
            final JPopupMenu menu, final boolean recursive)
    {
        return addMenuItems(null, menu, recursive);
    }

    public static final Collection<JMenuItem> setMenuItemsListener (
            final JPopupMenu menu, final ActionListener l)
    {
        if (null == l)
            return null;

        final Collection<JMenuItem>    ml=getMenuItems(menu, true);
        if ((null == ml) || (ml.size() <= 0))
            return null;

        for (final JMenuItem item : ml)
        {
            if (null == item)
                continue;
            item.addActionListener(l);
        }

        return ml;
    }

    public static final <B extends AbstractButton> B syncDisplayItem (AbstractButton source, B target)
    {
        if ((source == null) || (target == null))
            return target;

        {
            final Icon    curIcon=target.getIcon(), icon=source.getIcon();
            if ((curIcon == null) && (icon != null))
                target.setIcon(icon);
        }

        {
            final String    curText=target.getText(), text=source.getText();
            if (((curText == null) || (curText.length() <= 0)) && (text != null) && (text.length() > 0))
                target.setText(text);
        }

        {
            final String    curTooltip=target.getToolTipText(), tooltip=source.getToolTipText();
            if (((curTooltip == null) || (curTooltip.length() <= 0)) && (tooltip != null) && (tooltip.length() > 0))
                target.setToolTipText(tooltip);
        }

        return target;
    }

    public static final Map<String,Map.Entry<AbstractButton,AbstractButton>> syncDisplayedItems (
            final Map<String,? extends AbstractButton> buttonsMap, final Map<String,? extends AbstractButton> itemsMap)
    {
        if ((buttonsMap == null) || buttonsMap.isEmpty()
         || (itemsMap == null) || itemsMap.isEmpty())
            return null;

        Map<String,Map.Entry<AbstractButton,AbstractButton>>    ret=null;
        for (final Map.Entry<String,? extends AbstractButton> btnEntry : buttonsMap.entrySet())
        {
            final String            cmd=(btnEntry == null) ? null : btnEntry.getKey();
            final AbstractButton    btn=(btnEntry == null) ? null : btnEntry.getValue();
            final AbstractButton    itm=
                ((cmd == null) || (cmd.length() <= 0) || (btn == null)) ? null : itemsMap.get(cmd);
            if (itm == null)
                continue;

            if (syncDisplayItem(itm, btn) == null)
                continue;

            if (ret == null)
                ret = new TreeMap<String,Map.Entry<AbstractButton,AbstractButton>>(String.CASE_INSENSITIVE_ORDER);

            final Map.Entry<AbstractButton,AbstractButton>    pair=new MapEntryImpl<AbstractButton,AbstractButton>(itm, btn),
                                                            prev=ret.put(cmd, pair);
            if (prev != null)
                throw new IllegalStateException("Multiple mappings for cmd=" + cmd);
        }

        return ret;
    }

    public static final Map<String,Map.Entry<AbstractButton,AbstractButton>> syncFromMenuBar (
            final Map<String,? extends AbstractButton> buttonsMap, final JMenuBar menuBar)
    {
        return syncDisplayedItems(buttonsMap, getItemsMap(menuBar));
    }

    public static final Map<String,Map.Entry<AbstractButton,AbstractButton>> syncFromMenu (
            final Map<String,? extends AbstractButton> buttonsMap, final JMenu menu)
    {
        return syncDisplayedItems(buttonsMap, getItemsMap(menu));
    }

    public static final Map<String,Map.Entry<AbstractButton,AbstractButton>> syncFromPopupMenu (
            final Map<String,? extends AbstractButton> buttonsMap, final JPopupMenu menu)
    {
        return syncDisplayedItems(buttonsMap, getItemsMap(menu));
    }

    public static final JPopupMenu createPopupFromMenu (final JMenu menu)
    {
        final int    numItems=(menu == null) ? 0 : menu.getMenuComponentCount();
        if (numItems <= 0)
            return null;

        final JPopupMenu    pop=new JPopupMenu();
        for (int    index=0; index < numItems; index++)
        {
            final Component    org=menu.getMenuComponent(index),
                            cpy=cloneMenuComponent(org);
            if (cpy == null)
                continue;
            pop.add(cpy);
        }

        return pop;
    }

    public static final Component cloneMenuComponent (final Component org)
    {
        if (org == null)
            return null;

        if (org instanceof JMenuItem)
            return cloneMenuItem((JMenuItem) org);
        else if (org instanceof JSeparator)
            return cloneSeparator((JSeparator) org);
        else    // TODO clone a sub-menu
            throw new UnsupportedOperationException("Unknown menu component type to clone: " + org.getClass().getName());
    }

    public static final JMenuItem cloneMenuItem (final JMenuItem org)
    {
        if (org == null)
            return null;
        else
            return copyMenuItem(org, new JMenuItem());

    }

    public static final <M extends JMenuItem> M copyMenuItem (final JMenuItem src, final M dst)
    {
        if ((src == null) || (dst == null))
            return dst;

        {
            final String    text=src.getText();
            if (text != null)
                dst.setText(text);
        }

        {
            final String    cmd=src.getActionCommand();
            if (cmd != null)
                dst.setActionCommand(cmd);
        }

        {
            final Icon    icon=src.getIcon();
            if (icon != null)
                dst.setIcon(icon);
        }

        {
            final ActionListener[]    listeners=src.getActionListeners();
            if ((listeners != null) && (listeners.length > 0))
            {
                for (final ActionListener l : listeners)
                    dst.addActionListener(l);
            }
        }

        return dst;
    }

    public static final JSeparator cloneSeparator (final JSeparator org)
    {
        if (org == null)
            return null;
        else
            return new JSeparator(org.getOrientation());
    }
}
