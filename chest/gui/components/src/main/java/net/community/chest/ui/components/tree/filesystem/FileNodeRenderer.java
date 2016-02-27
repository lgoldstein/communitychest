/*
 * 
 */
package net.community.chest.ui.components.tree.filesystem;

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.community.chest.ui.helpers.tree.TypedTreeNode;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Displays file icon and other useful data using a {@link FileSystemView}
 * instance for it (default {@link FileSystemView#getFileSystemView()})</P>
 * 
 * @author Lyor G.
 * @since Aug 3, 2009 4:08:45 PM
 */
public class FileNodeRenderer extends DefaultTreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4196247978704671563L;
	private FileSystemView	_view;
	public FileSystemView getFileSystemView ()
	{
		return _view;
	}

	public void setFileSystemView (FileSystemView v)
	{
		_view = v;
	}

	public FileNodeRenderer (FileSystemView v)
	{
		_view = v;
	}

	public FileNodeRenderer ()
	{
		this(FileSystemView.getFileSystemView());
	}

	protected Icon setIcon (final Component c, final File f)
	{
		final FileSystemView	v=
			((null == f) || (c != this)) ? null : getFileSystemView();
		final Icon				i=
			(null == v) ? null : v.getSystemIcon(f);
		if (i != null)
			setIcon(i);
		return i;
	}

	protected String setText (final Component c, final File f)
	{
		final FileSystemView	v=
			((null == f) || (c != this)) ? null : getFileSystemView();
		final String			t=
			(null == v) ? null : v.getSystemDisplayName(f);
		if ((t != null) && (t.length() > 0))
			setText(t);
		return t;
	}

	protected String setToolTipText (final Component c, final File f)
	{
		final String	t=
			((null == f) || (c != this)) ? null : f.getAbsolutePath();
		if ((t != null) && (t.length() > 0))
			setToolTipText(t);

		return t;
	}
	/*
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent (JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean isFocused)
	{
		final Component	c=super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, isFocused);
		if (c != this)	// should not happen
			return c;

		if (!(value instanceof TypedTreeNode<?>))	// should not happen
			return c;

		final Object	o=((TypedTreeNode<?>) value).getAssignedValue();
		if (!(o instanceof File))	// should not happen
			return c;

		final File	f=(File) o;
		setText(c, f);
		setIcon(c, f);
		setToolTipText(c, f);
		return c;
	}
}
