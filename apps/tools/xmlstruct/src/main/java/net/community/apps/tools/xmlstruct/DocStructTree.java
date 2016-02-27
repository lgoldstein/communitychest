/*
 * 
 */
package net.community.apps.tools.xmlstruct;

import net.community.chest.swing.component.tree.BaseDefaultTreeModel;
import net.community.chest.swing.component.tree.TreeUtil;
import net.community.chest.ui.components.tree.document.DocumentTree;

import org.w3c.dom.Document;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 6, 2009 4:16:04 PM
 */
public class DocStructTree extends DocumentTree {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3781331315532137140L;
	public DocStructTree ()
	{
		setRootVisible(true);
	}
	/*
	 * @see net.community.chest.ui.components.tree.DocumentTree#showDocument(org.w3c.dom.Document)
	 */
	@Override
	protected void showDocument (final Document doc)
	{
		if (null == doc)
		{
			setModel(null);
			return;
		}

		final DocStructNode<Document>	root=new DocStructNode<Document>(Document.class, doc);
		setModel(new BaseDefaultTreeModel(root, true));
		setCellRenderer(new DOMStructNodeRenderer());
		TreeUtil.setNodesExpansionState(this, true);
	}
}
