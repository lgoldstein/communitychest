/*
 * 
 */
package net.community.apps.tools.adm.config;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.SortOrder;

import net.community.chest.ui.helpers.table.EnumColumnTableRowSorter;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 15, 2009 10:57:11 AM
 */
public class ValuesTableSorter
		extends EnumColumnTableRowSorter<ValuesTableColumn,ValueTableEntry,ValuesTableModel> {
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnTableRowSorter#resolveComparator(java.lang.Enum)
	 */
	@Override
	protected Comparator<?> resolveComparator (ValuesTableColumn colIndex)
	{
		if (null == colIndex)
			return null;
		// NOTE !!! this must be in sync with the model's getValueAt
		switch(colIndex)
		{
			case NAME	:
			case VALUE	:
			case SOURCE	:
				return String.CASE_INSENSITIVE_ORDER;
			default		:
				return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static final List<? extends Map.Entry<ValuesTableColumn,SortOrder>>
		DEFAULT_SORT_ORDER=
			Arrays.asList(
				new MapEntryImpl<ValuesTableColumn,SortOrder>(ValuesTableColumn.NAME, SortOrder.ASCENDING),
				new MapEntryImpl<ValuesTableColumn,SortOrder>(ValuesTableColumn.VALUE, SortOrder.ASCENDING),
				new MapEntryImpl<ValuesTableColumn,SortOrder>(ValuesTableColumn.SOURCE, SortOrder.ASCENDING)
			);

	public ValuesTableSorter (ValuesTableModel model)
	{
		super(model);
		addSortKeys(DEFAULT_SORT_ORDER);
	}
	
	public ValuesTableSorter ()
	{
		this(null);
	}
}
