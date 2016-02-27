/*
 * 
 */
package net.community.apps.tools.svn.wc;

import java.io.File;
import java.io.FileFilter;

import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableCellRenderer;

import net.community.apps.tools.svn.resources.DefaultResourcesAnchor;
import net.community.chest.svn.ui.filesmgr.SVNLocalCopyFileManagerModel;
import net.community.chest.svnkit.SVNFoldersFilter;
import net.community.chest.svnkit.core.wc.SVNLocalCopyData;
import net.community.chest.ui.components.table.file.FilesTableColumns;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 3:16:11 PM
 */
public class WCLocalFilesModel extends SVNLocalCopyFileManagerModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8517775317667782136L;
	private final WCMainFrame	_f;
	public final WCMainFrame getFrame ()
	{
		return _f;
	}

	public WCLocalFilesModel (WCMainFrame f, int initialSize)
	{
		super(initialSize);

		if (null == (_f=f))
			throw new IllegalArgumentException("No " + WCMainFrame.class.getSimpleName() + " instance provided");
	}

	public WCLocalFilesModel (WCMainFrame f)
	{
		this(f, 10);
	}

	private File	_parentFolder;
	public File getParentFolder ()
	{
		return _parentFolder;
	}

	public void setParentFolder (File d)
	{
		final File[]	fa;
		if (d != null)
		{
			if (!WCLocationFileInputVerifier.DEFAULT.verifyFile(d))
				return;

			fa = d.listFiles((FileFilter) SVNFoldersFilter.DEFAULT);
		}
		else
		{
			fa = File.listRoots();
		}

		clear(false);

		if (d != null)
			add(new SVNLocalCopyData(d), false);	// add parent entry


		if ((fa != null) && (fa.length > 0))
		{
			for (final File f : fa)
			{
				if (!SVNFoldersFilter.DEFAULT.accept(f))
					continue;
				add(new SVNLocalCopyData(f), false);
			}
		}

		_parentFolder = d;	// null == top-level

		final WCMainFrame	f=getFrame();
		/*
		 * NOTE !!! we rely on the fact that if this method was called from
		 * "setWCLocation" then it will not be called again since the
		 * reported location should match the one that we got as parameter
		 */
		f.setWCLocation(d, null, false);
		fireTableDataChanged();
	}

	public boolean isParentFolder (File f)
	{
		return AbstractComparator.compareObjects(getParentFolder(), f);
	}
	/*
	 * @see net.community.chest.svn.ui.filesmgr.SVNLocalCopyFileManagerModel#resolveColumnRenderer(net.community.chest.ui.components.table.file.FilesTableColumns, javax.swing.table.TableCellRenderer)
	 */
	@Override
	protected TableCellRenderer resolveColumnRenderer (FilesTableColumns colIndex, TableCellRenderer r)
	{
		if (FilesTableColumns.NAME.equals(colIndex) && (null == r))
			return new WCLocalFileCellRenderer(this, FileSystemView.getFileSystemView(), DefaultResourcesAnchor.getInstance().getStatusIconsMap());
		else if (FilesTableColumns.ATTRS.equals(colIndex) && (null == r))
			return new WCFileAttrsCellRenderer(DefaultResourcesAnchor.getInstance().getStatusIconsMap());
		else
			return super.resolveColumnRenderer(colIndex, r);
	}

	/*
	 * @see net.community.chest.svn.ui.filesmgr.SVNLocalCopyFileManagerModel#getColumnClass(net.community.chest.ui.components.table.file.FilesTableColumns)
	 */
	@Override
	public Class<?> getColumnClass (FilesTableColumns colIndex)
	{
		if (FilesTableColumns.ATTRS.equals(colIndex))
			return File.class;
		else
			return super.getColumnClass(colIndex);
	}
	/*
	 * @see net.community.chest.svn.ui.filesmgr.SVNLocalCopyFileManagerModel#getColumnValue(int, net.community.chest.svnkit.core.wc.SVNLocalCopyData, net.community.chest.ui.components.table.file.FilesTableColumns)
	 */
	@Override
	public Object getColumnValue (int rowIndex, SVNLocalCopyData row, FilesTableColumns colIndex)
	{
		if (FilesTableColumns.ATTRS.equals(colIndex))
			return row.getFile();
		else
			return super.getColumnValue(rowIndex, row, colIndex);
	}
}
