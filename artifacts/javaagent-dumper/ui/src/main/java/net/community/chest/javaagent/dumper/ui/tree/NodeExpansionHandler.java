/*
 * 
 */
package net.community.chest.javaagent.dumper.ui.tree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.community.chest.awt.attributes.Selectible;
import net.community.chest.javaagent.dumper.data.AbstractInfo;
import net.community.chest.javaagent.dumper.ui.data.SelectibleClassInfo;
import net.community.chest.javaagent.dumper.ui.data.SelectibleMethodInfo;
import net.community.chest.javaagent.dumper.ui.data.SelectiblePackageInfo;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 2:52:18 PM
 */
public class NodeExpansionHandler extends MouseAdapter implements TreeWillExpandListener, TreeExpansionListener, TreeSelectionListener {
	private final JTree	_tree;
	public NodeExpansionHandler (final JTree tree)
	{
		_tree = tree;
	}
	/*
	 * @see javax.swing.event.TreeWillExpandListener#treeWillExpand(javax.swing.event.TreeExpansionEvent)
	 */
	@Override
	public void treeWillExpand (TreeExpansionEvent event) throws ExpandVetoException
	{
		handleExpansionEvent(event);
	}
	/*
	 * @see javax.swing.event.TreeExpansionListener#treeExpanded(javax.swing.event.TreeExpansionEvent)
	 */
	@Override
	public void treeExpanded (TreeExpansionEvent event)
	{
		handleExpansionEvent(event);
	}
	/*
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	@Override
	public void valueChanged (TreeSelectionEvent e)
	{
		handleExpansionEvent(e.getNewLeadSelectionPath());
	}
	/*
	 * @see javax.swing.event.TreeWillExpandListener#treeWillCollapse(javax.swing.event.TreeExpansionEvent)
	 */
	@Override
	public void treeWillCollapse (TreeExpansionEvent event) throws ExpandVetoException
	{
		// ignored
	}
	/*
	 * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event.TreeExpansionEvent)
	 */
	@Override
	public void treeCollapsed (TreeExpansionEvent event)
	{
		// ignored
	}
	/* Interpret double click as collapse/expand - the opposite of whatever is
	 * ths current state of the selected node
	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked (final MouseEvent e)
	{
		if ((e == null) || (!SwingUtilities.isLeftMouseButton(e)) || (e.getClickCount() < 2))
			return;

		final TreePath	selPath=_tree.getSelectionPath();
		final TreeNode	selNode=(TreeNode) ((selPath == null) ? null : selPath.getLastPathComponent());
        if (selNode == null)
        	return;
        
        if (!_tree.isExpanded(selPath))
        	handleExpansionEvent(selPath);
	}

	protected void handleExpansionEvent (TreeExpansionEvent event)
	{
		handleExpansionEvent(event.getPath());
	}

	protected void handleExpansionEvent (final TreePath selPath)
	{
        final TreeNode	selNode=(TreeNode) ((selPath == null) ? null : selPath.getLastPathComponent());
        if (selNode == null)
        	return;

        final int		numChildren=selNode.getChildCount();
        if ((numChildren > 0) || (selNode instanceof MethodNode))
        	return;	// skip if already expanded or nothing to expand

        if (selNode instanceof PackageNode)
        	nodeStructureChanged(selPath, expandPackageNode((PackageNode) selNode));
        else if (selNode instanceof ClassNode)
        	nodeStructureChanged(selPath, expandClassNode((ClassNode) selNode));
	}

	protected PackageNode expandPackageNode (final PackageNode node)
	{
		final SelectiblePackageInfo		info=node.getAssignedValue();
		final List<SelectibleClassInfo>	classes=new ArrayList<SelectibleClassInfo>(info);
		Collections.sort(classes, SelectibleClassInfo.BY_SIMPLE_NAME_COMP);

		for (final SelectibleClassInfo clsInfo : classes)
		{
			if (!clsInfo.isPublic())	// show only public classes
				continue;
			node.add(new ClassNode(clsInfo));
		}

		return node;
	}

	protected ClassNode expandClassNode (final ClassNode node)
	{
		final SelectibleClassInfo			info=node.getAssignedValue();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<SelectibleMethodInfo>	methods=
				new ArrayList<SelectibleMethodInfo>((Collection) info.getMethods());
		Collections.sort(methods, AbstractInfo.BY_NAME_COMP);

		for (final SelectibleMethodInfo mthdInfo : methods)
		{
			if ((!mthdInfo.isPublic()) || mthdInfo.isConstructor())
				continue;	//skip constructors and non-public methods
			node.add(new MethodNode(mthdInfo));
		}

		return node;
	}
	
	protected <S extends Selectible, N extends AbstractInfoNode<S>> N nodeStructureChanged (
			final TreePath nodePath, final N node)
	{
		final DefaultTreeModel	model=(DefaultTreeModel) _tree.getModel();
		model.nodeStructureChanged(node);
		if (nodePath != null)
			_tree.expandPath(nodePath);
		return node;
	}
}
