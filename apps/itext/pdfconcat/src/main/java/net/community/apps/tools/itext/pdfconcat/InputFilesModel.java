/*
 * 
 */
package net.community.apps.tools.itext.pdfconcat;

import java.io.File;

import net.community.chest.CoVariantReturn;
import net.community.chest.ui.helpers.table.AbstractTypedTableModel;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 30, 2009 12:26:30 PM
 */
public class InputFilesModel extends AbstractTypedTableModel<File> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3982440385593278347L;
	public InputFilesModel ()
	{
		super(File.class);
	}
	/*
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount ()
	{
		return 1;
	}
	/*
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName (int column)
	{
		if (column != 0)
			return null;

		return "PDF Files";
	}
	/*
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass (int columnIndex)
	{
		if (columnIndex != 0)
			return Object.class;
		return File.class;
	}
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#getColumnValue(int, java.lang.Object, int)
	 */
	@Override
	@CoVariantReturn
	public File getColumnValue (int rowIndex, File row, int colIndex)
	{
		if (colIndex != 0)
			return null;

		return row;
	}
	/*
	 * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#setValueAt(int, java.lang.Object, int, java.lang.Object)
	 */
	@Override
	public void setValueAt (int rowIndex, File row, int colIndex, Object value)
	{
		throw new UnsupportedOperationException("setValueAt(" + row + ")[" + rowIndex + "," + colIndex + "]");
	}
	/*
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable (int rowIndex, int columnIndex)
	{
		return false;
	}
}
