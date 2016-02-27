/*
 * 
 */
package net.community.apps.tools.jgit.browser;

import java.awt.event.MouseEvent;

import javax.swing.JTree;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 20, 2011 10:57:08 AM
 */
class RefsTreeMouseAdapter extends AbstractTreeMouseAdapter {
	RefsTreeMouseAdapter (MainFrame frame, JTree invoker, Element elem) throws Exception
	{
		super(frame, invoker, elem);
	}
	/*
	 * @see net.community.apps.tools.jgit.browser.AbstractTreeMouseAdapter#getSelectedNodeId(java.lang.Object)
	 */
	@Override
	protected ObjectId getSelectedNodeId (final Object selNode)
	{
		final Ref	ref=(selNode instanceof RefNode) ? ((RefNode) selNode).getRef() : null;
		return (ref == null) ? null : ref.getObjectId();
	}
 	/*
 	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
 	 */
 	@Override
    public void mouseClicked (MouseEvent e)
 	{
		if (showPopupMenu(e) != null)
			return;
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
