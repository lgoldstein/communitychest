/*
 * 
 */
package net.community.chest.ui.components.tree.filesystem;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.swing.component.tree.BaseDefaultTreeModel;
import net.community.chest.swing.component.tree.BaseTree;
import net.community.chest.ui.helpers.SettableComponent;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 3, 2009 3:56:22 PM
 */
public class FolderFilesTree extends BaseTree
				implements SettableComponent<File>,
						   TypedComponentAssignment<File> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5957638945869474705L;
	public FolderFilesTree ()
	{
		super((TreeModel) null);
		setRootVisible(true);
	}

	private File	_f;
	/*
	 * @see net.community.chest.awt.TypedComponentAssignment#getAssignedValue()
	 */
	@Override
	public File getAssignedValue ()
	{
		return _f;
	}

	protected File resolveAssignedValue (File f)
	{
		if ((f != null) && (!f.isDirectory()))
			return f.getParentFile();

		return f;
	}
	/* NOTE: if set file is not a folder, then its parent is taken
	 * @see net.community.chest.awt.TypedComponentAssignment#setAssignedValue(java.lang.Object)
	 */
	@Override
	public void setAssignedValue (File f)
	{
		_f = resolveAssignedValue(f);
	}

	public File getFolder ()
	{
		return getAssignedValue();
	}

	protected Collection<File> getFolderFiles (final File f)
	{
		if ((null == f) || (!f.isDirectory()))
			return null;	// debug breakpoint

		final File[]	fl=f.listFiles();
		if ((null == fl) || (fl.length <= 0))
			return null;

		return Arrays.asList(fl);
	}

	protected MutableTreeNode createContainedFileNode (final File dir, final File f)
	{
		if ((null == dir) || (!dir.isDirectory()) || (null == f))
			return null;

		return new FileNode(f);
	}

	protected void showFolderFiles (final File f)
	{
		if ((null == f) || (!f.isDirectory()))
		{
			setModel(null);
			return;
		}

		final FileNode	root=new FileNode(f);
		setModel(new BaseDefaultTreeModel(root, true));
		setCellRenderer(new FileNodeRenderer());

		final Collection<? extends File>	fl=getFolderFiles(f);
		if ((null == fl) || (fl.size() <= 0))
			return;

		for (final File ff : fl)
		{
			final MutableTreeNode	fn=createContainedFileNode(f, ff);
			if (null == fn)
				continue;
			root.add(fn);
		}
	}

	public void setFolder (final File org, final boolean updateTree)
	{
		final File	f=resolveAssignedValue(org);
		setAssignedValue(f);

		if (updateTree)
			showFolderFiles(f);
	}
	/*
	 * @see net.community.chest.ui.helpers.SettableComponent#setContent(java.lang.Object)
	 */
	@Override
	public void setContent (File value)
	{
		setFolder(value);
	}

	public void setFolder (final File f)
	{
		setFolder(f, true);
	}
	/*
	 * @see net.community.chest.ui.helpers.SettableComponent#clearContent()
	 */
	@Override
	public void clearContent ()
	{
		setContent(null);
	}
	/*
	 * @see net.community.chest.ui.helpers.SettableComponent#refreshContent(java.lang.Object)
	 */
	@Override
	public void refreshContent (File value)
	{
		setContent(value);
	}
}
