/*
 * 
 */
package net.community.chest.svn.ui.filesmgr;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.SortOrder;

import net.community.chest.io.file.FileSizeComparator;
import net.community.chest.svnkit.core.wc.SVNLocalCopyData;
import net.community.chest.svnkit.core.wc.SVNLocalFileNameComparator;
import net.community.chest.swing.component.filechooser.FileTypeViewComparator;
import net.community.chest.ui.components.table.file.FilesTableColumns;
import net.community.chest.ui.helpers.table.EnumColumnTableRowSorter;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 10:14:51 AM
 */
public class SVNLocalCopyFileManagerSorter
		extends EnumColumnTableRowSorter<FilesTableColumns,SVNLocalCopyData,SVNLocalCopyFileManagerModel> {
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnTableRowSorter#resolveComparator(java.lang.Enum)
	 */
	@Override
	protected Comparator<?> resolveComparator (final FilesTableColumns colIndex)
	{
		if (null == colIndex)
			return null;
		// NOTE !!! this must be in sync with the model's getValueAt
		switch(colIndex)
		{
			case NAME	: return SVNLocalFileNameComparator.ASCENDING;
			case SIZE	: return FileSizeComparator.ASCENDING;
			case TYPE	: return FileTypeViewComparator.UPWARD;
			// NOTE - for last-modified-time we don't need a comparator since Date is Comparable...
			default		: return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static final List<? extends Map.Entry<FilesTableColumns,SortOrder>>
		DEFAULT_SORT_ORDER=
			Arrays.asList(
				new MapEntryImpl<FilesTableColumns,SortOrder>(FilesTableColumns.NAME, SortOrder.ASCENDING),
				new MapEntryImpl<FilesTableColumns,SortOrder>(FilesTableColumns.SIZE, SortOrder.ASCENDING)
			);

	public SVNLocalCopyFileManagerSorter (SVNLocalCopyFileManagerModel model)
	{
		super(model);
		addSortKeys(DEFAULT_SORT_ORDER);
	}
	
	public SVNLocalCopyFileManagerSorter ()
	{
		this(null);
	}
}
