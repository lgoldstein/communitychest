/*
 * 
 */
package net.community.chest.swing.component.tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright as per GPLv2</P>
 * Encapsulates the {@link TreeSelectionModel#setSelectionMode(int)} values as {@link Enum}
 * @author Lyor G.
 * @since Mar 16, 2011 2:07:13 PM
 */
public enum SelectionModelType {
	SINGLE(TreeSelectionModel.SINGLE_TREE_SELECTION),
	CONTIGUOUS(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION),
	DISCONTIGUOUS(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

	private final int	_mode;
	public final int getSelectionMode ()
	{
		return _mode;
	}
	
	SelectionModelType (final int mode)
	{
		_mode = mode;
	}

	public void setSelectionMode (final TreeSelectionModel model)
	{
		if (model != null)
			model.setSelectionMode(getSelectionMode());
	}
	
	public void setSelectionMode (final JTree tree)
	{
		if (tree != null)
			setSelectionMode(tree.getSelectionModel());
	}
	
	public static final List<SelectionModelType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final SelectionModelType fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}
	
	public static final SelectionModelType fromMode (final int mode)
	{
		for (final SelectionModelType v : VALUES)
		{
			if ((v != null) && (v.getSelectionMode() == mode))
				return v;
		}

		return null;
	}
	
	public static final SelectionModelType fromModel (final TreeSelectionModel model)
	{
		return (model == null) ? null : fromMode(model.getSelectionMode());
	}
	
	public static final SelectionModelType fromTree (final JTree tree)
	{
		return (tree == null) ? null : fromModel(tree.getSelectionModel());
	}
}
