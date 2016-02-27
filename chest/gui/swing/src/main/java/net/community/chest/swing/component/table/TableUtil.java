/*
 * 
 */
package net.community.chest.swing.component.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 22, 2009 9:40:40 AM
 */
public final class TableUtil {
	private TableUtil ()
	{
		// no instance
	}
	/**
	 * @param <TC> Type of {@link TableColumn} being processed
	 * @param colIndex The column <U>model index</U>
	 * @param cols A {@link Collection} of table columns to be scanned
	 * @return The first column whose {@link TableColumn#getModelIndex()}
	 * matches the required one - <code>null</code> if no match found 
	 */
	public static final <TC extends TableColumn> TC findTableColumn (final int colIndex, final Collection<? extends TC> cols)
	{
		if ((null == cols) || (cols.size() <= 0))
			return null;

		for (final TC tc : cols)
		{
			if ((tc != null) && (tc.getModelIndex() == colIndex))
				return tc;
		}

		return null;
	}

	public static final <TC extends TableColumn> TC findTableColumn (final int colIndex, final TC ... cols)
	{
		return ((null == cols) || (cols.length <= 0)) ? null : findTableColumn(colIndex, Arrays.asList(cols));
	}

	public static final TableColumn findTableColumn (
			final int colIndex, final TableColumnModel tcModel)
	{
		for (final Enumeration<? extends TableColumn>	cols=
				(null == tcModel) ? null : tcModel.getColumns();
			 (cols != null) && cols.hasMoreElements();
			 )
		{
			final TableColumn	tc=cols.nextElement();
			if ((tc != null) && (tc.getModelIndex() == colIndex))
				return tc;
		}

		return null;
	}
	/**
	 * @param <V> Type of expected value
	 * @param tbl The {@link JTable} instance to query
	 * @param model The model values to be used after calling
	 * {@link JTable#convertRowIndexToModel(int)}
	 * @return A {@link List} of the selected values - null/empty if no/bad
	 * selection
	 */
	public static final <V> List<V> getSelectedValues (
			final JTable tbl, final List<? extends V> model)
	{
		final int	selCount=(null == tbl) ? 0 : tbl.getSelectedRowCount();
		if (selCount <= 0)
			return null;

		final ListSelectionModel	sm=tbl.getSelectionModel();
		final int					selMode=
			(null == sm) ? (-1) : sm.getSelectionMode();
		final int[]	selRows;
		switch(selMode)
		{
			case ListSelectionModel.SINGLE_SELECTION	:
				selRows = new int[] { tbl.getSelectedRow() };
				break;
			case ListSelectionModel.MULTIPLE_INTERVAL_SELECTION	:
			case ListSelectionModel.SINGLE_INTERVAL_SELECTION	:
				selRows = tbl.getSelectedRows();
				break;
			default										:
				return null;	// should not happen
		}
		if ((null == selRows) || (selRows.length <= 0))
			return null;	// should not happen

		final int	numItems=(null == model) ? 0 : model.size();
		if (numItems <= 0)
			return null;	// should not happen

		List<V>	ret=null;
		for (final int	rIndex : selRows)
		{
			final int	mdlIndex=(rIndex < 0) ? (-1) : tbl.convertRowIndexToModel(rIndex);
			final V		v=
				((mdlIndex < 0) || (mdlIndex >= numItems)) ? null : model.get(mdlIndex);
			if (null == v)	// should not happen
				continue;

			if (null == ret)
				ret = new ArrayList<V>(selRows.length);
			ret.add(v);
		}

		return ret;
	}
}
