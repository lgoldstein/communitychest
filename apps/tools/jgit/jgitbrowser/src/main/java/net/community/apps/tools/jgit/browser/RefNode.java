/*
 * 
 */
package net.community.apps.tools.jgit.browser;

import java.awt.Component;
import java.awt.Font;
import java.awt.SystemColor;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;

import net.community.chest.awt.font.FontStyleType;
import net.community.chest.git.lib.ref.RefUtils;
import net.community.chest.ui.helpers.tree.TypedTreeNode;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 16, 2011 9:55:10 AM
 */
class RefNode extends TypedTreeNode<Ref> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 726035613922359347L;

	public RefNode (Ref ref)
	{
		super(Ref.class, ref, RefUtils.stripRefPath(ref));
	}

	public final Ref getRef ()
	{
		return getUserObject();
	}

	public static final TreeCellRenderer RENDERER=new DefaultTreeCellRenderer() {
			/**
		 * 
		 */
		private static final long serialVersionUID = -3481136088397250412L;

			/*
			 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
			 */
			@Override
			public Component getTreeCellRendererComponent (JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean isFocused)
			{
				final Component	c=super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, isFocused);
				if (sel)
				{
					c.setBackground(SystemColor.textHighlightText);
					c.setForeground(SystemColor.textHighlightText);
				}

				final Ref	ref=(value instanceof RefNode) ? ((RefNode) value).getRef() : null;
				if (ref == null)
					return c;
	
				final String	refName=ref.getName();
				if (Constants.HEAD.equals(refName))
				{
					final Font	orgFont=getFont(),
								newFont=orgFont.deriveFont(FontStyleType.BOLD.getStyleValue());
					setFont(newFont);
				}
				else
					setToolTipText(refName);
	
				return c;
			}
		};
}
