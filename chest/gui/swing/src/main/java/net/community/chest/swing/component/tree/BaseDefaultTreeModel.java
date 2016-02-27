package net.community.chest.swing.component.tree;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import net.community.chest.CoVariantReturn;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides some extra useful functionality for {@link DefaultTreeModel}</P>
 * 
 * @author Lyor G.
 * @since Jul 29, 2007 9:52:21 AM
 */
public class BaseDefaultTreeModel extends DefaultTreeModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3636753099962426221L;
	public BaseDefaultTreeModel (TreeNode rootNode)
	{
		super(rootNode);
	}

	public BaseDefaultTreeModel (TreeNode rootNode, boolean allowsChildren)
	{
		super(rootNode, allowsChildren);
	}
	/**
	 * Helper class that casts a node {@link Object} into a {@link TreeNode}
	 * derived class per request - used by {@link #getRoot()} override
	 * @param <N> The {@link TreeNode} type being used
	 * @param nodeClass expected node class - may NOT be null
	 * @param node node object
	 * @return cast node object - may be null if original node was null 
	 * @throws ClassCastException if cannot cast the node
	 */
	public static final <N extends TreeNode> N getNode (final Class<N> nodeClass, final Object node) throws ClassCastException
	{
		if (null == nodeClass)
			throw new ClassCastException(ClassUtil.getArgumentsExceptionLocation(BaseDefaultTreeModel.class, "getRoot", node) + " no node class specified");
		if (null == node)
			return null;

		return nodeClass.cast(node);
	}
	/*
	 * @see javax.swing.tree.DefaultTreeModel#getRoot()
	 */
	@Override
	@CoVariantReturn
	public TreeNode getRoot ()
	{
		return getNode(TreeNode.class, super.getRoot());
	}
}
