/*
 * 
 */
package net.community.chest.javaagent.dumper.ui.tree;

import net.community.chest.awt.attributes.Selectible;
import net.community.chest.ui.helpers.tree.TypedTreeNode;

/**
 * <P>Copyright as per GPLv2</P>
 * @param <V> Type of {@link Selectible} information being held in the node
 * @author Lyor G.
 * @since Aug 17, 2011 3:15:24 PM
 */
public abstract class AbstractInfoNode<V extends Selectible> extends TypedTreeNode<V> implements Selectible {
	private static final long serialVersionUID = -3234951879572559090L;
	protected AbstractInfoNode (Class<V> nodeClass, V nodeObject, String nodeText, boolean withChildren)
	{
		super(nodeClass, nodeObject, nodeText, withChildren);
	}
	/*
	 * @see net.community.chest.awt.attributes.Selectible#isSelected()
	 */
	@Override
	public boolean isSelected ()
	{
		final Selectible	selValue=getAssignedValue();
		if (selValue == null)
			return false;

		return selValue.isSelected();
	}
	/*
	 * @see net.community.chest.awt.attributes.Selectible#setSelected(boolean)
	 */
	@Override
	public void setSelected (boolean v)
	{
		final Selectible	selValue=getAssignedValue();
		if (selValue == null)
			return;
		selValue.setSelected(v);
	}

}
