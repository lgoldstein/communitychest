/*
 * 
 */
package net.community.chest.svn.ui.filesmgr;

import javax.swing.table.TableColumnModel;

import net.community.chest.svnkit.core.wc.SVNLocalCopyData;
import net.community.chest.ui.components.table.file.FilesTableColumns;
import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;
import net.community.chest.ui.helpers.table.EnumColumnTypedTable;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 2:00:52 PM
 */
public class SVNLocalCopyFileManagerTable extends EnumColumnTypedTable<FilesTableColumns,SVNLocalCopyData> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8431218505000335954L;

	public SVNLocalCopyFileManagerTable (EnumColumnAbstractTableModel<FilesTableColumns,SVNLocalCopyData> tbModel, TableColumnModel tcModel)
	{
		super(tbModel, tcModel);
	}

	public SVNLocalCopyFileManagerTable (EnumColumnAbstractTableModel<FilesTableColumns,SVNLocalCopyData> model)
	{
		super(model, (null == model) ? null : model.getTableColumnModel());
	}

	public SVNLocalCopyFileManagerTable ()
	{
		this(new SVNLocalCopyFileManagerModel());
	}
}
