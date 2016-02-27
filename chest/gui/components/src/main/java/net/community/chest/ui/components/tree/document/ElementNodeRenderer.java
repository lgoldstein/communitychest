/*
 * 
 */
package net.community.chest.ui.components.tree.document;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.community.chest.dom.DOMUtils;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 2, 2008 8:04:17 AM
 */
public class ElementNodeRenderer extends DefaultTreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3834573142794627996L;
	public ElementNodeRenderer ()
	{
		super();
	}
	/*
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent (JTree tree,
			Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean isFocused)
	{
		final Component	c=super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, isFocused);
		if (value instanceof ElementNode)
		{
			final Element	elem=((ElementNode) value).getElement();
			final String	text=DOMUtils.toString(elem);
			if ((text != null) && (text.length() > 0))
				setToolTipText(text);
		}

		return c;
	}
}
