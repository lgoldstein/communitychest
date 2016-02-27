/*
 * 
 */
package net.community.apps.tools.jgit.browser;

import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import net.community.chest.git.lib.GitlibUtils;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.map.MapEntryImpl;

import org.eclipse.jgit.lib.FileTreeEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.TreeEntry;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 21, 2011 11:00:13 AM
 *
 */
class FilesTreeMouseAdapter extends AbstractTreeMouseAdapter {
	FilesTreeMouseAdapter (MainFrame frame, JTree invoker, Element elem) throws Exception
	{
		super(frame, invoker, elem);
	}
	/*
	 * @see net.community.apps.tools.jgit.browser.AbstractTreeMouseAdapter#getSelectedNodeId(java.lang.Object)
	 */
	@Override
	protected ObjectId getSelectedNodeId (Object selNode)
	{
		final TreeEntry	entry=(selNode instanceof TreeEntryNode) ? ((TreeEntryNode) selNode).getTreeEntry() : null;
		return (entry == null) ? null : entry.getId();
	}

	protected File openSelectedTreeEntry ()
	{
		final JTree						invoker=getInvoker();
        final TreePath 					selPath=
        	(invoker == null) ? null : invoker.getSelectionPath();
        final Object					selNode=
        	(null == selPath) /* OK if nothing chosen */ ? null : selPath.getLastPathComponent() ;
        final TreeEntry					entry=
        	(selNode instanceof TreeEntryNode) ? ((TreeEntryNode) selNode).getTreeEntry() : null;
        final Map.Entry<File,Boolean>	selFile=resolveSelectedFile(entry);
		final File						file=(selFile == null) ? null : selFile.getKey();
		if ((file == null) || (!file.exists()) || (!file.isFile()))
		{
			JOptionPane.showMessageDialog(invoker, "Cannot retrieve file data", "Data unavailable", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}

		final Desktop	d=Desktop.getDesktop();
		try
		{
			d.open(file);
		}
		catch(IOException e)
		{
			BaseOptionPane.showMessageDialog(invoker, e);
		}
		finally
		{
			final Boolean	delAfterEdit=(selFile == null) ? null : selFile.getValue();
			if ((delAfterEdit != null) && delAfterEdit.booleanValue()
			 && file.isFile() && (!file.delete()))
				JOptionPane.showMessageDialog(invoker, "Failed to delete temporary file", "Operation failed", JOptionPane.WARNING_MESSAGE);
		}

		return file;
	}

	protected Map.Entry<File,Boolean> resolveSelectedFile (final TreeEntry	entry)
	{
		File	file=GitlibUtils.getTreeEntryLocation(entry);
		if ((file != null) && file.exists() && file.isFile())
			return new MapEntryImpl<File,Boolean>(file, Boolean.FALSE);

		if (!(entry instanceof FileTreeEntry))
			return null;

		try
		{
			if ((file=GitlibUtils.dumpToTempFile((FileTreeEntry) entry)) != null) 
				return new MapEntryImpl<File,Boolean>(file, Boolean.TRUE);
		}
		catch(IOException e)
		{
			BaseOptionPane.showMessageDialog(getInvoker(), e);
		}

		return null;
	}
 	/*
 	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
 	 */
 	@Override
    public void mouseClicked (MouseEvent e)
 	{
		if (showPopupMenu(e) != null)
			return;

		// ignore double click if already running
 		if ((null == e) || (e.getClickCount() <= 1))
 			return;
         
 		if (!SwingUtilities.isLeftMouseButton(e))
 			return;

 		openSelectedTreeEntry();
 	}
	/*
	 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed (MouseEvent e)
	{
		if (showPopupMenu(e) != null)
			return;
	}
	/*
	 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased (MouseEvent e)
	{
		if (showPopupMenu(e) != null)
			return;
	}
}
