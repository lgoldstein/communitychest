/*
 * 
 */
package net.community.apps.tools.svn.wc;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.community.apps.tools.svn.wc.state.PropertiesStateChecker;
import net.community.apps.tools.svn.wc.state.ShowLogStateChecker;
import net.community.apps.tools.svn.wc.state.StateChecker;
import net.community.chest.svnkit.core.wc.SVNLocalCopyData;
import net.community.chest.swing.component.button.ButtonUtils;
import net.community.chest.swing.component.menu.BasePopupMenu;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 9, 2009 10:29:03 AM
 */
public class WCFilesManagerPopupMenu extends BasePopupMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6304387430405668549L;
	public WCFilesManagerPopupMenu (Element elem) throws Exception
	{
		super(elem);
	}

	private static final Collection<? extends StateChecker>	CHECKERS=
		Arrays.asList(
			PropertiesStateChecker.DEFAULT,
			ShowLogStateChecker.DEFAULT
				);
	public void updateMenuItemsState (final List<? extends SVNLocalCopyData> selValues)
	{
		Map<String,Boolean>	sm=null;
		for (final StateChecker c : CHECKERS)
		{
			final String	cmd=(null == c) ? null : c.getActionCommand();
			final Boolean	v=
				((null == cmd) || (cmd.length() <= 0)) ? null : Boolean.valueOf(c.checkState(selValues));
			if (null == v)
				continue;

			if (null == sm)
				sm = new TreeMap<String,Boolean>(String.CASE_INSENSITIVE_ORDER);

			final Boolean	prev=sm.put(cmd, v);
			if ((prev != null) && (!prev.equals(v)))
				throw new IllegalStateException("updateMenuItemsState(" + cmd + ") mismatched values: cur=" + v + "/prev=" + prev);
		}

		ButtonUtils.updateButtonsStates(getItemsMap(), sm);
	}

	public void updateMenuItemsState (final WCLocalFilesManager t)
	{
		updateMenuItemsState((null == t) ? null : t.getSelectedValues()); 
	}
}
