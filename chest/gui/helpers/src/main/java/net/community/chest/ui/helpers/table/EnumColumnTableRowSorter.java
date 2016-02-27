/*
 * 
 */
package net.community.chest.ui.helpers.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.SortOrder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <E> The {@link Enum} used for columns
 * @param <V> The value assigned to each row
 * @param <M> The {@link EnumColumnAbstractTableModel} used for the table
 * 
 * @author Lyor G.
 * @since Mar 22, 2009 10:06:31 AM
 */
public class EnumColumnTableRowSorter<E extends Enum<E>,V,M extends EnumColumnAbstractTableModel<E,V>> extends TableRowSorter<M> {
	// returns null by default unless overridden - i.e., same as "super.getComparator"
	public Comparator<?> getComparator (E colIndex)
	{
		final M				m=(null == colIndex) ? null : getModel();
		final TableColumn	tc=(null == m) ? null : m.getColumnInfo(colIndex);
		if (null == tc)
			return null;

		return super.getComparator(tc.getModelIndex());
	}

	public EnumTableColumn<E> getColumnInfo (final E colIndex)
	{
		final M	m=getModel();
		return (null == m) ? null : m.getColumnInfo(colIndex);
	}

	public void setComparator (E colIndex, Comparator<?> comparator)
	{
		final TableColumn	tc=getColumnInfo(colIndex);
		if (tc != null)
			setComparator(tc.getModelIndex(), comparator);
	}
	// called by constructor for initial population of the comparator(s)
	protected Comparator<?> resolveComparator (final E colIndex)
	{
		return (null == colIndex) ? null : null;
	}

	public E getColumnIndex (int column)
	{
		final M		m=(column < 0) ? null : getModel();
		return (null == m) ? null : m.getColumnValue(column);
	}
	// returns column model index (negative if error)
	public int getColumnModelIndex (final E colIndex)
	{
		final M	m=(null == colIndex) ? null : getModel();
		return (null == m) ? (-1) : m.getColumnModelIndex(colIndex);
	}
	// returns column model index (negative if error)
	public int setSortable (E colIndex, boolean sortable)
	{
		final int	column=getColumnModelIndex(colIndex);
		if (column >= 0)
			setSortable(column, sortable);
		return column;
	}
	// returns null if error
	public Boolean isSortable (E colIndex)
	{
		final int	column=getColumnModelIndex(colIndex);
		if (column >= 0)
			return Boolean.valueOf(isSortable(column));
		else
			return null;
	}
	// returns column model index (negative if error)
    public int toggleSortOrder (E colIndex)
    {
		final int	column=getColumnModelIndex(colIndex);
		if (column >= 0)
			toggleSortOrder(column);
		return column;
    }

	public SortKey addSortKey (E colIndex, SortOrder o)
	{
		final int	column=(null == o) ? (-1) : getColumnModelIndex(colIndex);
		if (column <= 0)
			return null;

		final Collection<? extends SortKey>	kl=getSortKeys();
		final int							numKeys=(null == kl) ? 0 : kl.size();
		final SortKey						sk=new SortKey(column, o);
		final List<SortKey>					nl;
		// need to execute a copy since the internal value is unmodifiable
		if (numKeys > 0)
		{
			nl = new ArrayList<SortKey>(numKeys + 1);
			nl.addAll(kl);
			nl.add(sk);
		}
		else
			nl = Arrays.asList(sk);

		setSortKeys(nl);
		return sk;
	}

	public List<SortKey> addSortKeys (Collection<? extends Map.Entry<E,SortOrder>> ol)
	{
		final int	numAdded=(null == ol) ? 0 : ol.size();
		if (numAdded <= 0)
			return null;

		List<SortKey>	nl=null;
		for (final Map.Entry<E,SortOrder>	oe : ol)
		{
			final E			colIndex=(null == oe) ? null : oe.getKey();
			final SortOrder	o=(null == oe) ? null : oe.getValue();
			final int		column=
				(null == o) ? (-1) : getColumnModelIndex(colIndex);
			if (column < 0)
				continue;

			if (null == nl)
			{
				final Collection<? extends SortKey>	kl=getSortKeys();
				final int							numKeys=
					(null == kl) ? 0 : kl.size();
				nl = new ArrayList<SortKey>(Math.max(0, numKeys) + numAdded);
				nl.addAll(kl);
			}

			nl.add(new SortKey(column, o));
		}

		return nl;
	}

	public List<SortKey> addSortKeys (Map.Entry<E,SortOrder> ... ol)
	{
		return ((null == ol) || (ol.length <= 0)) ? null : addSortKeys(Arrays.asList(ol));
	}

	public EnumColumnTableRowSorter (M model)
	{
		super(model);

		final Collection<? extends E>	cols=(null == model) ? null : model.getColumnsValues();
		if ((cols == null) || cols.isEmpty())
			return;

		for (final E colIndex : cols)
		{
			final Comparator<?>	c=resolveComparator(colIndex);
			if (null == c)
				continue;
			setComparator(colIndex, c);
		}
	}

	public EnumColumnTableRowSorter ()
	{
		this(null);
	}
	/*
	 * @see javax.swing.table.TableRowSorter#getComparator(int)
	 */
	@Override
	public Comparator<?> getComparator (int column)
	{
		final M				m=getModel();
		final E				col=(null == m) ? null : m.getColumnValue(column);
		final Comparator<?>	c=getComparator(col);
		if (c != null)
			return c;

		return super.getComparator(column);
	}
	/**
	 * Returns the full row value on any invocation of {@link #getValueAt(int, int)}
	 * or {@link #getIdentifier(int)}
	 * @param <TV> The type of object associated with a row 
	 * @param <TM> The type of underlying {@link AbstractTypedTableModel}
	 */
	public static class FullRowTableModelWrapper<TV,TM extends AbstractTypedTableModel<TV>> extends ModelWrapper<TM,Integer> {
		private TM	_model;
		/*
		 * @see javax.swing.DefaultRowSorter.ModelWrapper#getModel()
		 */
		@Override
		public TM getModel ()
		{
			return _model;
		}

		public void setModel (TM m)
		{
			_model = m;
		}
		/*
		 * @see javax.swing.DefaultRowSorter.ModelWrapper#getColumnCount()
		 */
		@Override
		public int getColumnCount ()
		{
			final TableModel	m=getModel();
	        return (m == null) ? 0 : m.getColumnCount();
	    }
		/*
		 * @see javax.swing.DefaultRowSorter.ModelWrapper#getRowCount()
		 */
		@Override
		public int getRowCount ()
	    {
			final TableModel	m=getModel();
	        return (m == null) ? 0 : m.getRowCount();
		}
		/*
		 * @see javax.swing.DefaultRowSorter.ModelWrapper#getIdentifier(int)
		 */
		@Override
		public Integer getIdentifier (int row)
		{
			return Integer.valueOf(row);
		}
		/*
		 * @see javax.swing.DefaultRowSorter.ModelWrapper#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt (int row, int column)
		{
			final AbstractTypedTableModel<TV>	m=getModel();
			return (null == m) ? null : m.get(row);
		}

		public FullRowTableModelWrapper (TM m)
		{
			_model = m;
		}
	}

	public boolean isFullRowModelWrapper ()
	{
		final ModelWrapper<?,?>	w=getModelWrapper();
		return (w instanceof FullRowTableModelWrapper<?,?>);
	}

	public void setFullRowModelWrapper (boolean useFull)
	{
		final M	m=getModel();
		if (useFull)
			setModelWrapper(new FullRowTableModelWrapper<V,M>(m));
		else if (isFullRowModelWrapper())
			setModel(m);
	}

	public List<E> getColumnsValues ()
	{
		final M	m=getModel();
		return (null == m) ? null : m.getColumnsValues();
	}

	public void setFullRowComparator (final Comparator<V> c)
	{
		final Collection<? extends E>	cols=getColumnsValues();
		if ((null == cols) || (cols.size() <= 0))
			return;

		for (final E col : cols)
			setComparator(col, c);
	}
}
