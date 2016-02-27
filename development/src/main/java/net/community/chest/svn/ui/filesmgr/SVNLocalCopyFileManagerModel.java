/*
 * 
 */
package net.community.chest.svn.ui.filesmgr;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.table.TableCellRenderer;

import net.community.chest.lang.math.SizeUnits;
import net.community.chest.svnkit.core.wc.SVNLocalCopyData;
import net.community.chest.swing.HAlignmentValue;
import net.community.chest.ui.components.table.file.FileAttrsCellRenderer;
import net.community.chest.ui.components.table.file.FileDateTimeCellRenderer;
import net.community.chest.ui.components.table.file.FileSizeCellRenderer;
import net.community.chest.ui.components.table.file.FileTypeCellRenderer;
import net.community.chest.ui.components.table.file.FilesTableColInfo;
import net.community.chest.ui.components.table.file.FilesTableColumns;
import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;
import net.community.chest.ui.helpers.table.EnumTableColumn;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 1:36:51 PM
 */
public class SVNLocalCopyFileManagerModel
		extends EnumColumnAbstractTableModel<FilesTableColumns,SVNLocalCopyData>
		implements ISVNStatusHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6632723312636998851L;
	public SVNLocalCopyFileManagerModel (final int initialSize)
	{
		super(FilesTableColumns.class, SVNLocalCopyData.class, initialSize);
		setColumnsValues(FilesTableColumns.VALUES);
	}

	public SVNLocalCopyFileManagerModel ()
	{
		this(10);
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#fromColumnElement(org.w3c.dom.Element)
	 */
	@Override
	public EnumTableColumn<FilesTableColumns> fromColumnElement (Element colElem) throws Exception
	{
		return new FilesTableColInfo(colElem);
	}
	// NOTE !!! set renderer must match value returned by "getColumnValue"
	protected TableCellRenderer resolveColumnRenderer (
			final FilesTableColumns colIndex, final TableCellRenderer r)
	{
		if ((null == colIndex) || (r != null))
			return r;

		switch(colIndex)
		{
			case NAME		:	return new SVNLocalCopyFileNameRenderer();
			case TYPE		:	return new FileTypeCellRenderer();
			case MODTIME	:	return new FileDateTimeCellRenderer(new SimpleDateFormat("dd-MMM-yyyy HH:mm"));
			case SIZE		:	return new FileSizeCellRenderer(SizeUnits.KB, false, HAlignmentValue.RIGHT);
			case ATTRS		:	return new FileAttrsCellRenderer();
			default			:
				throw new IllegalArgumentException("addColumn(" + colIndex + ") unknown column");
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#addColumn(net.community.chest.ui.helpers.table.EnumTableColumn)
	 */
	@Override
	public Map<FilesTableColumns,EnumTableColumn<FilesTableColumns>> addColumn (EnumTableColumn<FilesTableColumns> col)
	{
		final FilesTableColumns	colIndex=(null == col) ? null : col.getColumnValue();
		if (null == colIndex)
			throw new IllegalArgumentException("addColumn(" + col + ") no column index");

		TableCellRenderer	r=resolveColumnRenderer(colIndex, col.getCellRenderer());
		if (r != null)
			col.setCellRenderer(r);
		return super.addColumn(col);
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#getColumnClass(java.lang.Enum)
	 */
	@Override
	public Class<?> getColumnClass (FilesTableColumns colIndex)
	{
		if (null == colIndex)
			return super.getColumnClass(colIndex);

		// NOTE !!! returned type must match value returned by "getColumnValue"
		switch(colIndex)
		{
			case NAME		:	return SVNLocalCopyData.class;
			case MODTIME	:	return Date.class;
			case SIZE		:	return File.class;
			case TYPE		:	return File.class;
			case ATTRS		:	return Collection.class;
			default			: 
				throw new NoSuchElementException("getColumnClass(" + colIndex + ") unknown column requested");
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#getColumnValue(int, java.lang.Object, java.lang.Enum)
	 */
	@Override
	public Object getColumnValue (int rowIndex, SVNLocalCopyData row, FilesTableColumns colIndex)
	{
		if (null == colIndex)
			throw new IllegalStateException("getColumnValue(" + rowIndex + ") no column");

		final File	f=(null == row) ? null : row.getFile();
		if (null == f)
			throw new IllegalStateException("getColumnValue(" + rowIndex + "/" + colIndex + ") no row data");

		// NOTE !!! returned type must match getColumnClass() report
		switch(colIndex)
		{
			case NAME		:	return row;
			case MODTIME	:	return new Date(f.lastModified());
			case SIZE		:	return f;
			case TYPE		:	return f;
			case ATTRS		:	return FileAttrsCellRenderer.getFileAttributes(f);
			default			: 
				throw new NoSuchElementException("getColumnValue(" + rowIndex + "/" + colIndex + ")[" + row + "] unknown column requested");
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#setValueAt(int, java.lang.Object, int, java.lang.Enum, java.lang.Object)
	 */
	@Override
	public void setValueAt (int rowIndex, SVNLocalCopyData row, int colNum, FilesTableColumns colIndex, Object value)
	{
		throw new UnsupportedOperationException("setValueAt(" + row + ":" + colNum + ")[" + colIndex + "]@" + rowIndex + "=" + value + " - N/A");
	}

	public int findIndexByFile (final File f)
	{
		final int	numItems=size();
		if ((null == f) || (numItems <= 0))
			return (-1);

		for (int	index=0; index < numItems; index++)
		{
			final SVNLocalCopyData	lclData=get(index);
			final File				lclFile=
				(null == lclData) ? null : lclData.getFile();
			if (f.equals(lclFile))
				return index;
		}

		return (-1);
	}

	public SVNLocalCopyData findByFile (final File f)
	{
		final int	index=findIndexByFile(f);
		if ((index < 0) || (index >= size()))
			return null;

		return get(index);
	}

	public SVNLocalCopyData handleStatus (final SVNStatus status, final boolean fireEvent)
	{
		final File				f=(null == status) ? null : status.getFile();
		final int				index=findIndexByFile(f);
		final SVNLocalCopyData	lclData=
			((index < 0) || (index >= size())) ? null : get(index);
		if (null == lclData)
			return null;

		// don't update the table view if nothing changed even if required to fire event
		if (lclData.fromSVNStatus(status) && fireEvent)
			fireTableRowsUpdated(index, index);
		return lclData;
	}
	/*
	 * @see org.tmatesoft.svn.core.wc.ISVNStatusHandler#handleStatus(org.tmatesoft.svn.core.wc.SVNStatus)
	 */
	@Override
	public void handleStatus (final SVNStatus status) throws SVNException
	{
		handleStatus(status, true);
	}
}
