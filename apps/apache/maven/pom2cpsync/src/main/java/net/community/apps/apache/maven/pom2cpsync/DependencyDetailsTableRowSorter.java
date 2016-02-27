/*
 * 
 */
package net.community.apps.apache.maven.pom2cpsync;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

import net.community.chest.apache.maven.helpers.GroupIdComparator;
import net.community.chest.util.compare.VersionComparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 17, 2008 8:42:18 AM
 */
public class DependencyDetailsTableRowSorter extends TableRowSorter<DependencyDetailsTableModel> {
	public DependencyDetailsTableRowSorter (DependencyDetailsTableModel model)
	{
		super(model);
		setMaxSortKeys(DependencyDetailsColumns.VALUES.size());
	}
	/*
	 * @see javax.swing.table.TableRowSorter#getComparator(int)
	 */
	@Override
	public Comparator<?> getComparator (final int column)
	{
		final DependencyDetailsTableModel	m=getModel();
		final DependencyDetailsColumns		colType=(null == m) ? null : m.getColumnValue(column);
		if (DependencyDetailsColumns.GROUP.equals(colType))
			return GroupIdComparator.ASCENDING;
		else if (DependencyDetailsColumns.VERSION.equals(colType))
			return VersionComparator.ASCENDING;

		return super.getComparator(column);
	}
	/* Use 1st call to set default sort rows
	 * @see javax.swing.DefaultRowSorter#getSortKeys()
	 */
	@Override
	public List<? extends SortKey> getSortKeys ()
	{
		final DependencyDetailsTableModel	m=getModel();
		final List<? extends SortKey>		curKeys=super.getSortKeys();
		if ((null == m) || ((curKeys != null) && (curKeys.size() > 0)))
			return curKeys;

		// NOTE !!! assumption is that columns index matches ordinal value
		final List<SortKey>	sortKeys=new ArrayList<SortKey>(getMaxSortKeys());
		for (final DependencyDetailsColumns	colType : DependencyDetailsColumns.VALUES)
		{
			final int	colIndex=colType.ordinal();
			sortKeys.add(new SortKey(colIndex, SortOrder.ASCENDING));
		}
		setSortKeys(sortKeys);

		return super.getSortKeys();
	}
}
