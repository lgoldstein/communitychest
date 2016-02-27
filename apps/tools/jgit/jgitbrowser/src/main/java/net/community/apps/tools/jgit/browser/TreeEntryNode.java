/*
 * 
 */
package net.community.apps.tools.jgit.browser;

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import net.community.chest.git.lib.GitlibUtils;
import net.community.chest.ui.helpers.tree.TypedTreeNode;

import org.eclipse.jgit.lib.Tree;
import org.eclipse.jgit.lib.TreeEntry;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 16, 2011 12:37:04 PM
 */
public class TreeEntryNode extends TypedTreeNode<TreeEntry> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7349642082258001397L;

	public TreeEntryNode (TreeEntry entry)
	{
		super(TreeEntry.class, entry, entry.getName(), (entry instanceof Tree));
	}

	public final TreeEntry getTreeEntry ()
	{
		return getUserObject();
	}

	public static final TreeCellRenderer RENDERER=new DefaultTreeCellRenderer() {
			/**
		 * 
		 */
		private static final long serialVersionUID = 7904414024376898150L;

			/*
			 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
			 */
			@Override
			public Component getTreeCellRendererComponent (JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean isFocused)
			{
				final Component		c=super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, isFocused);
				final TreeEntry		entry=(value instanceof TreeEntryNode) ? ((TreeEntryNode) value).getTreeEntry() : null;
				final File			file=GitlibUtils.getTreeEntryLocation(entry);
				if (file == null)
					return c;

				setToolTipText(file.getAbsolutePath());
	
				final FileSystemView	view=FileSystemView.getFileSystemView();
				final Icon				icon=(view == null) ? null : view.getSystemIcon(file);
				if (icon != null)
					setIcon(icon);
				return c;
			}
		};
}
