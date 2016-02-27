/*
 * 
 */
package net.community.chest.swing.component.menu;

import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JMenu;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <M> The mapped {@link JMenu}
 * @author Lyor G.
 * @since Aug 27, 2008 1:28:35 PM
 */
public class MenusMap<M extends JMenu> extends TreeMap<String,M> implements MenuExplorer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4086748320462097050L;
	public MenusMap (Comparator<? super String> comparator)
	{
		super(comparator);
	}

	// NOTE: default=case INSENSITIVE
	public MenusMap ()
	{
		this(String.CASE_INSENSITIVE_ORDER);
	}

	public MenusMap (Map<? extends String,? extends M> m)
	{
		this(String.CASE_INSENSITIVE_ORDER);
		putAll(m);
	}

	public MenusMap (SortedMap<String,? extends M> m)
	{
		super(m);
	}
	/*
	 * @see net.community.chest.swing.component.menu.MenuExplorer#findMenuByCommand(java.lang.String)
	 */
	@Override
	public M findMenuByCommand (final String cmd)
	{
		if ((null == cmd) || (cmd.length() <= 0))
			return null;

		return get(cmd);
	}
	/*
	 * @see net.community.chest.swing.component.menu.MenuExplorer#getMenusMap()
	 */
	@Override
	public Map<String,? extends JMenu> getMenusMap ()
	{
		return this;
	}
	/*
	 * @see net.community.chest.swing.component.menu.MenuExplorer#resetMenusMap()
	 */
	@Override
	public void resetMenusMap ()
	{
		clear();
	}
	/*
	 * @see net.community.chest.swing.component.menu.MenuExplorer#addMenuActionListenerByCommand(java.lang.String, java.awt.event.ActionListener, boolean)
	 */
	@Override
	public JMenu addMenuActionListenerByCommand (String cmd, ActionListener l, boolean recursive)
	{
		return MenuUtil.addMenuActionHandler(this, cmd, l, recursive);
	}
}
