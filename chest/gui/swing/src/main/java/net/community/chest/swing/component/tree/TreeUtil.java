package net.community.chest.swing.component.tree;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful {@link JTree} manipulation methods</P>
 *
 * @author Lyor G.
 * @since Jul 31, 2007 4:15:22 PM
 */
public final class TreeUtil {
    private TreeUtil ()
    {
        // no instance
    }
    /**
     * <P>Sets all nodes expansion/collapse state as required - starting from
     * specified parent node (inclusive).</P>
     * @param tree {@link JTree} whose nodes are to be expanded/collapsed
     * @param parent parent node under which all nodes must be expanded/collapsed
     * (inclusive)
     * @param expanded TRUE=expand, FALSE=collapse
     * @return number of affected nodes (zero if null/empty tree/parent)
     */
    public static final int setNodesExpansionState (final JTree tree, final TreePath parent, final boolean expanded)
    {
        if (null == tree)
            return 0;

        // Expansion or collapse must be done bottom-up
        final Object    nodeObject=(null == parent) ? null : parent.getLastPathComponent();
        int                numNodes=0;
        if ((nodeObject != null) && (nodeObject instanceof TreeNode))
        {
            final TreeNode    node=(TreeNode) nodeObject;
            if (node.getChildCount() >= 0)
            {
                for (final Enumeration<?> e=node.children(); (e != null) && e.hasMoreElements(); )
                {
                    final Object    child=e.nextElement();
                    if ((null == child) || (!(child instanceof TreeNode)))
                        continue;    // should not happen

                    numNodes += setNodesExpansionState(tree, parent.pathByAddingChild(child), expanded);
                }
            }
        }

        if (parent != null)
        {
            if (expanded)
                tree.expandPath(parent);
            else
                tree.collapsePath(parent);

            numNodes++;
        }

        return numNodes;
    }
    /**
     * <P>Sets all nodes expansion/collapse state as required</P>
     * @param tree {@link JTree} whose nodes are to be expanded/collapsed
     * @param expanded TRUE=expand, FALSE=collapse
     * @return number of affected nodes (zero if null/empty tree)
     */
    public static final int setNodesExpansionState (final JTree tree, final boolean expanded)
    {
        final TreeModel    model=(null == tree) ? null : tree.getModel();
        final Object    root=(null == model) ? null : model.getRoot();
        if ((null == root) || (!(root instanceof TreeNode)))
            return 0;

        return setNodesExpansionState(tree, new TreePath(root), expanded);
    }

    public static final TreeNode[] findNodePath (final JTree tree, final TreeNode node)
    {
        return findNodePath((tree == null) ? null : tree.getModel(), node);
    }

    public static final TreeNode[] findNodePath (final TreeModel model, final TreeNode node)
    {
        return (model instanceof DefaultTreeModel) ? ((DefaultTreeModel) model).getPathToRoot(node) : null;
    }

    public static final TreePath findNodeTreePath (final JTree tree, final TreeNode node)
    {
        return findNodeTreePath((tree == null) ? null : tree.getModel(), node);
    }

    public static final TreePath findNodeTreePath (final TreeModel model, final TreeNode node)
    {
        final TreeNode[]    path=findNodePath(model, node);
        if ((path == null) || (path.length <= 0))
            return null;

        return new TreePath(path);
    }
    /**
     * @param tree The {@link JTree} whose currently selected node is to be set
     * @param node The {@link TreeNode} instance to set as currently selected
     * @return The select node {@link TreePath} - <code>null</code> if selection
     * not changed
     */
    public static final TreePath setSelectedNode (final JTree tree, final TreeNode node)
    {
        final TreePath                path=findNodeTreePath(tree, node);
        final TreeSelectionModel    model=tree.getSelectionModel();
        if ((model == null) || (path == null))
            return null;

        model.setSelectionPath(path);
        return path;
    }
}
