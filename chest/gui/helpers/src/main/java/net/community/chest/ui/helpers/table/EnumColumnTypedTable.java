/*
 * 
 */
package net.community.chest.ui.helpers.table;

import java.util.List;
import java.util.Map;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <E> The {@link Enum} type used for columns
 * @param <V> The value associated with each row
 * @author Lyor G.
 * @since Mar 22, 2009 9:04:12 AM
 */
public class EnumColumnTypedTable<E extends Enum<E>,V> extends TypedTable<V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6450802680723099736L;
	public EnumColumnTypedTable (EnumColumnAbstractTableModel<E,V> tbModel, TableColumnModel tcModel)
	{
		super(tbModel, tcModel);
	}

	public EnumColumnTypedTable (EnumColumnAbstractTableModel<E,V> model)
	{
		this(model, (null == model) ? null : model.getTableColumnModel());
	}
	/*
	 * @see net.community.chest.ui.helpers.table.TypedTable#getTypedModel()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public EnumColumnAbstractTableModel<E,V> getTypedModel ()
	{
		return (EnumColumnAbstractTableModel<E,V>) super.getTypedModel();
	}

	public Class<E> getColumnClass ()
	{
		final EnumColumnAbstractTableModel<E,?>	m=getTypedModel();
		return (null == m) ? null : m.getColumnClass();
	}

	public List<E> getColumnsValues ()
	{
		final EnumColumnAbstractTableModel<E,?>	m=getTypedModel();
		return (null == m) ? null : m.getColumnsValues();
	}

	public EnumTableColumn<E> getColumnInfo (final E colIndex)
	{
		final EnumColumnAbstractTableModel<E,?>	m=getTypedModel();
		return (null == m) ? null : m.getColumnInfo(colIndex);
	}
	/*
	 * @see javax.swing.JTable#addColumn(javax.swing.table.TableColumn)
	 */
	@Override
	public void addColumn (TableColumn column)
	{
		if (!(column instanceof EnumTableColumn<?>))
			throw new IllegalArgumentException("addColumn(" + column + ") not a " + EnumTableColumn.class.getSimpleName());

		final EnumColumnAbstractTableModel<E,?>	m=getTypedModel();
		if (m != null)
		{
			@SuppressWarnings("unchecked")
			final EnumTableColumn<E>	colInfo=(EnumTableColumn<E>) column;
			m.addColumn(colInfo);
		}

		super.addColumn(column);
	}

	public E getColumnIndex (final int column)
	{
		final EnumColumnAbstractTableModel<E,?>	m=getTypedModel();
		if (m != null)
		{
			final int	colIndex=convertColumnIndexToModel(column);
			return m.getColumnValue(colIndex);
		}
		return null;	// debug breakpoint
	}

	protected Map<E,TableCellEditor> getColEditorsMap (final boolean createIfNotExist)
	{
		return super.getColEditorsMap(getColumnClass(), createIfNotExist);
	}
	/*
	 * @see javax.swing.JTable#getCellEditor(int, int)
	 */
	@Override
	public TableCellEditor getCellEditor (int row, int column)
	{
		return getCellEditor(row, column, getColumnIndex(column));
	}

	protected Map<E,TableCellRenderer> getColRenderersMap (final boolean createIfNotExist)
	{
		return super.getColRenderersMap(getColumnClass(), createIfNotExist);
	}

	/*
	 * @see javax.swing.JTable#getCellRenderer(int, int)
	 */
	@Override
	public TableCellRenderer getCellRenderer (int row, int column)
	{
		return getCellRenderer(row, column, getColumnIndex(column));
	}

	public boolean isCellEditable (int row, int column, E colIndex)
	{
		return (colIndex != null) && super.isCellEditable(row, column);
	}
	/*
	 * @see javax.swing.JTable#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable (int row, int column)
	{
		return isCellEditable(row, column, getColumnIndex(column));
	}

	public boolean isCellSelected (int row, int column, E colIndex)
	{
		return (colIndex != null) && super.isCellSelected(row, column);
	}
	/*
	 * @see javax.swing.JTable#isCellSelected(int, int)
	 */
	@Override
	public boolean isCellSelected (int row, int column)
	{
		return isCellSelected(row, column, getColumnIndex(column));
	}

	public boolean isColumnSelected (int column, E colIndex)
	{
		return (colIndex != null) && super.isColumnSelected(column);
	}
	/*
	 * @see javax.swing.JTable#isColumnSelected(int)
	 */
	@Override
	public boolean isColumnSelected (int column)
	{
		return isColumnSelected(column, getColumnIndex(column));
	}
}
