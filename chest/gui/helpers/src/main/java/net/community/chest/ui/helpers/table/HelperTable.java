/*
 * 
 */
package net.community.chest.ui.helpers.table;

import java.util.EnumMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.community.chest.swing.component.table.BaseTable;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 7:26:43 AM
 */
public class HelperTable extends BaseTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5436787466276478368L;
	public HelperTable (TableModel dm, TableColumnModel cm, ListSelectionModel sm)
	{
		super(dm, cm, sm);
	}

	public HelperTable (TableModel dm, TableColumnModel cm)
	{
		this(dm, cm, null);
	}

	public HelperTable (TableModel dm)
	{
		this(dm, (TableColumnModel) null);
	}

	public HelperTable ()
	{
		this((TableModel) null);
	}

	public HelperTable (int numRows, int numColumns)
	{
		super(numRows, numColumns);
	}

	public HelperTable (Vector<?> rowData, Vector<?> columnNames)
	{
		super(rowData, columnNames);
	}

	public HelperTable (Object[][] rowData, Object[] columnNames)
	{
		super(rowData, columnNames);
	}
	private Map<Enum<?>,TableCellEditor>	_colEditorsMap;
	@SuppressWarnings({ "unchecked", "cast", "rawtypes" })
	protected <E extends Enum<E>> Map<E,TableCellEditor> getColEditorsMap (
			final Class<E> colClass, final boolean createIfNotExist)
	{
		if ((null == _colEditorsMap) && createIfNotExist)
			_colEditorsMap = new EnumMap(colClass);
		return (Map<E,TableCellEditor>) ((Map) _colEditorsMap);
	}

	protected <E extends Enum<E>> Map<E,TableCellEditor> getColEditorsMap (final Class<E> colClass)
	{
		return getColEditorsMap(colClass, false);
	}

	@SuppressWarnings({ "unchecked", "cast", "rawtypes" })
	protected <E extends Enum<E>> void setColEditorsMap (Map<E,TableCellEditor> m)
	{
		_colEditorsMap = (Map<Enum<?>,TableCellEditor>) ((Map) m);
	}

	public <E extends Enum<E>> TableCellEditor getColCellEditor (E colIndex)
	{
		@SuppressWarnings("unchecked")
		final Map<E,? extends TableCellEditor>	m=
			(null == colIndex) ? null : getColEditorsMap(colIndex.getClass());
		if ((null == m) || (m.size() <= 0))
			return null;

		return m.get(colIndex);
	}
	// returns previous instance (if any)
	public <E extends Enum<E>> TableCellEditor setColCellEditor (
			final E colIndex, final TableCellEditor e /* null == remove */)
	{
		if (null == colIndex)
			return null;

		@SuppressWarnings("unchecked")
		final Map<E,TableCellEditor>	m=getColEditorsMap(colIndex.getClass(), e != null);
		if (null == e)	// asked to remove
		{
			if ((null == m) || (m.size() <= 0))
				return null;
			return m.remove(colIndex);
		}

		return m.put(colIndex, e);
	}

	public <E extends Enum<E>> TableCellEditor getCellEditor (
			final int row, final int column, final E colIndex)
	{
		final TableCellEditor	e=getColCellEditor(colIndex);
		if (e != null)
			return e;

		return super.getCellEditor(row, column);
	}

	private Map<Enum<?>,TableCellRenderer>	_colRenderersMap;
	@SuppressWarnings({ "unchecked", "cast", "rawtypes" })
	protected <E extends Enum<E>> Map<E,TableCellRenderer> getColRenderersMap (
			final Class<E> colClass, final boolean createIfNotExist)
	{
		if ((null == _colRenderersMap) && createIfNotExist)
			_colRenderersMap = new EnumMap(colClass);
		return (Map<E,TableCellRenderer>) ((Map) _colRenderersMap);
	}

	protected <E extends Enum<E>> Map<E,TableCellRenderer> getColRenderersMap (final Class<E> colClass)
	{
		return getColRenderersMap(colClass, false);
	}

	@SuppressWarnings({ "unchecked", "cast", "rawtypes" })
	protected <E extends Enum<E>> void setColRenderersMap (Map<E,TableCellRenderer> m)
	{
		_colRenderersMap = (Map<Enum<?>,TableCellRenderer>) ((Map) m);
	}

	public <E extends Enum<E>> TableCellRenderer getColCellRenderer (E colIndex)
	{
		@SuppressWarnings("unchecked")
		final Map<E,? extends TableCellRenderer>	m=
			(null == colIndex) ? null : getColRenderersMap(colIndex.getClass());
		if ((null == m) || (m.size() <= 0))
			return null;

		return m.get(colIndex);
	}
	// returns previous instance (if any)
	public <E extends Enum<E>> TableCellRenderer setColCellRenderer (
			final E colIndex, final TableCellRenderer r /* null == remove */)
	{
		if (null == colIndex)
			return null;

		@SuppressWarnings("unchecked")
		final Map<E,TableCellRenderer>	m=getColRenderersMap(colIndex.getClass(), r != null);
		if (null == r)	// asked to remove
		{
			if ((null == m) || (m.size() <= 0))
				return null;
			return m.remove(colIndex);
		}

		return m.put(colIndex, r);
	}

	public <E extends Enum<E>>  TableCellRenderer getCellRenderer (
			final int row, final int column, final E colIndex)
	{
		final TableCellRenderer	r=getColCellRenderer(colIndex);
		if (r != null)
			return r;
		
		return super.getCellRenderer(row, column);
	}
}
