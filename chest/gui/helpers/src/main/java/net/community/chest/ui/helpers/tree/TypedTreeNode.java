package net.community.chest.ui.helpers.tree;

import java.util.Comparator;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import net.community.chest.CoVariantReturn;
import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to give some type safety to {@link DefaultMutableTreeNode}</P>
 * 
 * @param <V> The generic node type 
 * @author Lyor G.
 * @since Jul 29, 2007 9:17:16 AM
 */
public class TypedTreeNode<V> extends DefaultMutableTreeNode
		implements TypedValuesContainer<V>, TypedComponentAssignment<V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4634814896589916280L;
	private final Class<V>	_nodeClass;
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final /* no cheating */ Class<V> getValuesClass ()
	{
		return _nodeClass;
	}

	private String	_nodeText	/* =null */;
	/**
	 * @return associated node text to display 
	 */
	public String getNodeText ()
	{
		return _nodeText;
	}

	public void setNodeText (String nodeText)
	{
		_nodeText = nodeText;
	}

	public TypedTreeNode (Class<V> nodeClass, V nodeObject, String nodeText, boolean withChildren)
	{
		super(nodeObject, withChildren);

		if (null == (_nodeClass=nodeClass))
			throw new IllegalArgumentException("No node class specified");

		if (null == (_nodeText=nodeText))
			_nodeText = getNodeText(nodeObject);
	}

	public TypedTreeNode (Class<V> nodeClass, V nodeObject, boolean withChildren)
	{
		this(nodeClass, nodeObject, null, withChildren);
	}

	public TypedTreeNode (Class<V> nodeClass, V nodeObject, String nodeText)
	{
		this(nodeClass, nodeObject, nodeText, true);
	}

	public TypedTreeNode (Class<V> nodeClass, V nodeObject)
	{
		this(nodeClass, nodeObject, null);
	}

	public TypedTreeNode (Class<V> nodeClass)
	{
		this(nodeClass, null);
	}
	/*
	 * @see javax.swing.tree.DefaultMutableTreeNode#getUserObject()
	 */
	@Override
	@CoVariantReturn
	public V getUserObject ()
	{
		return getValuesClass().cast(super.getUserObject());
	}
	/**
	 * @param nodeObject associated node object (may be null)
	 * @param nodeText text to be displayed for the object
	 */
	public void setUserObject (final V nodeObject, final String nodeText)
	{
		super.setUserObject(nodeObject);
		setNodeText(nodeText);
	}
	/**
	 * Called by {@link #setUserObject(Object)} to determine associated
	 * node text to be displayed
	 * @param nodeObject original node object - may be null
	 * @return text to be displayed
	 * @see #setUserObject(Object, String) as recommended usage
	 */
	public String getNodeText (final V nodeObject)
	{
		return (null == nodeObject) ? null : nodeObject.toString();
	}
	/*
	 * @see javax.swing.tree.DefaultMutableTreeNode#setUserObject(java.lang.Object)
	 */
	@Override
	public void setUserObject (final Object nodeObject)
	{
		final Class<?>	objClass=(null == nodeObject) /* OK */ ? null : nodeObject.getClass();
		if (objClass != null)
		{
			final Class<V>	nodeClass=getValuesClass();
			if (!nodeClass.isAssignableFrom(objClass))
				throw new ClassCastException(ClassUtil.getExceptionLocation(getClass(), "setUserObject") + " incompatible types: expected=" + nodeClass.getName() + ";got=" + objClass.getName());

			final V	effObject=nodeClass.cast(nodeObject);
			setUserObject(effObject, getNodeText(effObject));
		}
		else
			setUserObject(null, getNodeText(null));
	}
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#getAssignedValue()
	 */
	@Override
	public V getAssignedValue ()
	{
		return getUserObject();
	}
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#setAssignedValue(java.lang.Object)
	 */
	@Override
	public void setAssignedValue (V value)
	{
		setUserObject(value);
	}
	/*
	 * @see javax.swing.tree.DefaultMutableTreeNode#toString()
	 */
	@Override
	public String toString ()
	{
		return getNodeText();
	}
	/**
	 * @param <V> Type of value being held in the {@link TypedTreeNode}
	 * @param <N> Type of {@link TypedTreeNode} being scanned
	 * @param tree The underlying {@link JTree}
	 * @param nodeClass The expected node {@link Class} - any nodes not of
	 * this type are ignored
	 * @param value The value to compare with in order to decide if node
	 * matches
	 * @param c The {@link Comparator} to use to decide if values are the
	 * same. If <code>null</code> then {@link Object#equals(Object)} is used.
	 * @return The <U>first</U> node (including the root) whose value matches
	 * the provided one - <code>null</code> if no match found
	 * @see #findNode(TreeModel, Class, Object, Comparator)
	 */
	public static final <V, N extends TypedTreeNode<V>> N findNode (
			final JTree tree, final Class<N> nodeClass, final V value, final Comparator<? super V> c)
	{
		return findNode((tree == null) ? null : tree.getModel(), nodeClass, value, c);
	}
	/**
	 * @param <V> Type of value being held in the {@link TypedTreeNode}
	 * @param <N> Type of {@link TypedTreeNode} being scanned
	 * @param model The underlying {@link TreeModel}
	 * @param nodeClass The expected node {@link Class} - any nodes not of
	 * this type are ignored
	 * @param value The value to compare with in order to decide if node
	 * matches
	 * @param c The {@link Comparator} to use to decide if values are the
	 * same. If <code>null</code> then {@link Object#equals(Object)} is used.
	 * @return The <U>first</U> node (including the root) whose value matches
	 * the provided one - <code>null</code> if no match found
	 * @see #findNode(TreeModel, Object, Class, Object, Comparator)
	 */
	public static final <V, N extends TypedTreeNode<V>> N findNode (
			final TreeModel model, final Class<N> nodeClass, final V value, final Comparator<? super V> c)
	{
		return findNode(model, (model == null) ? null : model.getRoot(), nodeClass, value, c);
	}
	/**
	 * @param <V> Type of value being held in the {@link TypedTreeNode}
	 * @param <N> Type of {@link TypedTreeNode} being scanned
	 * @param model The underlying {@link TreeModel}
	 * @param root The root object from which to start looking
	 * @param nodeClass The expected node {@link Class} - any nodes not of
	 * this type are ignored
	 * @param value The value to compare with in order to decide if node
	 * matches
	 * @param c The {@link Comparator} to use to decide if values are the
	 * same. If <code>null</code> then {@link Object#equals(Object)} is used.
	 * @return The <U>first</U> node (including the root) whose value matches
	 * the provided one - <code>null</code> if no match found
	 */
	public static final <V, N extends TypedTreeNode<V>> N findNode (
			final TreeModel model, final Object root, final Class<N> nodeClass, final V value, final Comparator<? super V> c)
	{
		if (root == null)
			return null;

		final Class<?>	rootClass=root.getClass();
		if (nodeClass.isAssignableFrom(rootClass))
		{
			final N	node=nodeClass.cast(root);
			final V	nodeValue=node.getUserObject();
			if (value == nodeValue)
				return node;

			if (c != null)
			{
				final int	nRes=c.compare(value, nodeValue);
				if (nRes == 0)
					return node;
			}
			else if ((nodeValue != null) && nodeValue.equals(value))
				return node;
		}

		final int	nCount=(model == null) ? 0 : model.getChildCount(root);
		if (nCount <= 0)
			return null;

		for (int	cIndex=0; cIndex < nCount; cIndex++)
		{
			final Object	child=model.getChild(root, cIndex);
			final N			node=findNode(model, child, nodeClass, value, c);
			if (node != null)
				return node;
		}

		return null;
	}
}
