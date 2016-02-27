/*
 * 
 */
package net.community.apps.tools.svn.wc;

import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.SwingUtilities;

import net.community.chest.svnkit.core.wc.SVNLocalCopyData;
import net.community.chest.swing.options.BaseOptionPane;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 12:49:47 PM
 */
public class WCFilesManagerMouseAdapter extends MouseAdapter {
	private final WCLocalFilesManager	_m;
	public final WCLocalFilesManager getManager ()
	{
		return _m;
	}

	private final WCFilesManagerPopupMenu	_pm;
	public final WCFilesManagerPopupMenu getPopupMenu ()
	{
		return _pm;
	}

	public WCFilesManagerMouseAdapter (
			final WCLocalFilesManager m, final WCFilesManagerPopupMenu pm)
	{
		if (null == (_m=m))
			throw new IllegalStateException("No " + WCLocalFilesManager.class.getSimpleName() + " instance provided");
		_pm = pm;
	}

	public void handleSelectedFileLaunch ()
	{
		final WCLocalFilesManager				t=getManager();
		final List<? extends SVNLocalCopyData>	sv=
			(null == t) ? null : t.getSelectedValues(); 
		final int					selCount=
			(null == sv) ? 0 : sv.size();
		final SVNLocalCopyData		lclData=
			(1 == selCount) ? sv.get(0) : null;
		final File					f=
			(null == lclData) ? null :lclData.getFile();
		if (null == f)
			return;

		if (f.isFile())
		{
			final Desktop	d=Desktop.getDesktop();
			try
			{
				d.edit(f);
			}
			catch(Exception e)
			{
				BaseOptionPane.showMessageDialog(t, e);
			}
		}
		else if (f.isDirectory())
		{
			final WCLocalFilesModel	m=t.getTypedModel();
			if (m.isParentFolder(f))
				m.setParentFolder(f.getParentFile());
			else
				m.setParentFolder(f);
		}
	}

	protected boolean showPopupMenu (MouseEvent e)
	{
		if ((null == e) || (!e.isPopupTrigger()))
			return false;

		final WCLocalFilesManager		mgr=getManager(); 
		final WCFilesManagerPopupMenu	pm=(null == mgr) ? null : getPopupMenu();
		if (pm != null)
		{
			pm.updateMenuItemsState(mgr);
			pm.show(mgr, e.getX(), e.getY());
		}

		return true;
	}
 	/*
 	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
 	 */
 	@Override
    public void mouseClicked (MouseEvent e)
 	{
 		if ((null == e) || showPopupMenu(e))
 			return;
 		
 		if (SwingUtilities.isLeftMouseButton(e))
 		{
 			if (e.getClickCount() > 1)
 				handleSelectedFileLaunch();
 		}
 	}
	/*
	 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed (MouseEvent e)
	{
		if (showPopupMenu(e))
			return;
	}
	/*
	 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased (MouseEvent e)
	{
		if (showPopupMenu(e))
			return;
	}
}
