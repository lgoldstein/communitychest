package net.community.chest.swing.component.menu;

import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JMenuItem;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Interface implemented by various containers of {@link JMenuItem}-s that
 * enables locating items by various criteria.</P>
 *
 * @author Lyor G.
 * @since Jul 25, 2007 1:21:14 PM
 */
public interface MenuItemExplorer {
    /**
     * Locates a {@link JMenuItem} in the container given its action command
     * {@link String} value
     * @param cmd item action command - <B>Note:</B> handling of null/empty
     * command string(s) is up to the implementation, though recommended
     * behavior is to avoid supporting such commands
     * @return located item - null if none found.
     */
    JMenuItem findMenuItemByCommand (String cmd);
    /**
     * @return {@link Map} of currently contained {@link JMenuItem}-s, where
     * key=action command string, value=associated item. May be null/empty if
     * no current items in container - or only separators</P>
     *
     * <P>Note(s):</P>
     * <UL>
     *         <LI>
     *         <P>Case sensitivity of search string is left to implementation,
     *         though recommended implementation is to use <B><U>unique</U></B>
     *         case <U>insensitive</U> strings.</P>
     *         </LI></BR>
     *
     *         <LI>
     *         <P>Map contents are assumed to be <U>lazy-initialized</U> which
     *         means that if items/menus were added <U>after</U> this call, then
     *         one must call {@link #resetItemsMap()} prior to calling this
     *         method.</P>
     *         </LI></BR>
     *
     *         <LI>
     *         <P>{@link javax.swing.JMenu} components are ignored even though they are
     *         derived from {@link JMenuItem}-s.</P>
     *         </LI></BR>
     * </UL
     */
    Map<String,? extends JMenuItem> getItemsMap ();
    /**
     * Should be called if changes may have occurred since
     * {@link #getItemsMap()} was last called
     */
    void resetItemsMap ();
    /**
     * Adds an {@link ActionListener} to a {@link JMenuItem} given its
     * action command string.
     * @param cmd item action command - <B>Note:</B> handling of null/empty
     * command string(s) is up to the implementation, though recommended
     * behavior is to avoid supporting such commands
     * @param listener listener instance to be added - ignored if null
     * @return item to which listener was added - null if no match found
     * (or if null listener instance)
     */
    JMenuItem addItemActionListenerByCommand (String cmd, ActionListener listener);
}
