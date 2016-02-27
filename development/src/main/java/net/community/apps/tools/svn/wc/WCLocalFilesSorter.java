/*
 * 
 */
package net.community.apps.tools.svn.wc;

import java.util.Comparator;

import net.community.chest.svn.ui.filesmgr.SVNLocalCopyFileManagerSorter;
import net.community.chest.ui.components.table.file.FilesTableColumns;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 11:03:11 AM
 */
public class WCLocalFilesSorter extends SVNLocalCopyFileManagerSorter {
	public WCLocalFilesSorter (WCLocalFilesModel m)
	{
		super(m);
	}

	public WCLocalFilesModel getTypedModel ()
	{
		return (WCLocalFilesModel) super.getModel();
	}
	/*
	 * @see net.community.chest.svn.ui.filesmgr.SVNLocalCopyFileManagerSorter#resolveComparator(net.community.chest.ui.components.table.file.FilesTableColumns)
	 */
	@Override
	protected Comparator<?> resolveComparator (FilesTableColumns colIndex)
	{
		if (FilesTableColumns.NAME.equals(colIndex))
			return new WCLocalFileNameComparator(getTypedModel(), true);

		return super.resolveComparator(colIndex);
	}
}
