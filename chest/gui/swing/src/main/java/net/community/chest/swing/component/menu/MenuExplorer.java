/**
 * 
 */
package net.community.chest.swing.component.menu;

import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JMenu;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * * <P>Interface implemented by various containers of {@link JMenu}-s that
 * enables locating sub-menus by various criteria.</P>

 * @author Lyor G.
 * @since Jul 31, 2008 3:15:35 PM
 */
public interface MenuExplorer {
	/**
	 * Locates a {@link JMenu} in the container given its action command
	 * {@link String} value
	 * @param cmd item action command - <B>Note:</B> handling of null/empty
	 * command string(s) is up to the implementation, though recommended
	 * behavior is to avoid supporting such commands
	 * @return located item - null if none found.
	 */
	JMenu findMenuByCommand (String cmd);
	/**
	 * @return {@link Map} of currently contained {@link JMenu}-s, where
	 * key=action command string, value=associated item. May be null/empty if
	 * no current items in container - or only separators</P>
	 * 
	 * <P>Note(s):</P>
	 * <UL>
	 * 		<LI>
	 * 		<P>Case sensitivity of search string is left to implementation,
	 * 		though recommended implementation is to use <B><U>unique</U></B>
	 * 		case <U>insensitive</U> strings.</P>
	 * 		</LI></BR>
	 * 
	 * 		<LI>
	 * 		<P>Map contents are assumed to be <U>lazy-initialized</U> which
	 * 		means that if items/menus were added <U>after</U> this call, then
	 * 		one must call {@link #resetMenusMap()} prior to calling this
	 * 		method.</P>
	 * 		</LI></BR>
	 * </UL
	 */
	Map<String,? extends JMenu> getMenusMap ();
	/**
	 * Should be called if changes may have occurred since
	 * {@link #getMenusMap()} was last called
	 */
	void resetMenusMap ();
	/**
	 * Adds an {@link ActionListener} to a {@link JMenu} its given its
	 * action command string.
	 * @param cmd item action command - <B>Note:</B> handling of null/empty
	 * command string(s) is up to the implementation, though recommended
	 * behavior is to avoid supporting such commands
	 * @param listener listener instance to be added - ignored if null
	 * @param recursive TRUE=add this action listener to all sub-items
	 * of the menu (if found) - useful if same handler is used for all items
	 * of the menu
	 * @return item to which listener was added - null if no match found
	 * (or if null listener instance)
	 */
	JMenu addMenuActionListenerByCommand (String cmd, ActionListener listener, boolean recursive);

}
