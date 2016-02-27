/*
 * 
 */
package net.community.apps.common.test.table;

import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 22, 2009 8:30:20 AM
 */
public class TestTableModel extends EnumColumnAbstractTableModel<TestTableColumnType,TestTableRowData> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7194407512810758439L;
	private static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(TestTableModel.class);
	public TestTableModel ()
	{
		super(TestTableColumnType.class, TestTableRowData.class, TestTableColumnType.VALUES);
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#getColumnValue(int, java.lang.Object, java.lang.Enum)
	 */
	@Override
	public Object getColumnValue (int rowIndex, TestTableRowData row, TestTableColumnType colIndex)
	{
		final Object	value=((null == row) || (null == colIndex) || (rowIndex < 0)) ? null : row.get(colIndex);
		return "[" + String.valueOf(rowIndex) + "," + ((null == colIndex) ? (-1) : colIndex.ordinal()) + "]" + ((null == value) ?  "" : "=" + value);
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#setValueAt(int, java.lang.Object, int, java.lang.Enum, java.lang.Object)
	 */
	@Override
	public void setValueAt (int rowIndex, TestTableRowData row, int colNum, TestTableColumnType colIndex, Object value)
	{
		if ((rowIndex < 0) || (null == row) || (null == colIndex))
			throw new IllegalArgumentException("setValueAt(" + rowIndex + ":" + colNum + "/" + colIndex + ") bad arguments");

		final Object	prev;
		if (null == value)
			prev = row.remove(colIndex);
		else
			prev = row.put(colIndex, value);
		_logger.info("setValueAt(" + rowIndex + ":" + colNum + "/" + colIndex + ")[" + prev + "] => " + value);
	}
}
