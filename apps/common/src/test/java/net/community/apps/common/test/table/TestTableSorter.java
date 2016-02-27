/*
 * 
 */
package net.community.apps.common.test.table;

import net.community.chest.ui.helpers.table.EnumColumnTableRowSorter;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 22, 2009 10:55:52 AM
 */
public class TestTableSorter extends EnumColumnTableRowSorter<TestTableColumnType,TestTableRowData,TestTableModel> {
	public TestTableSorter (TestTableModel model)
	{
		super(model);

		setFullRowModelWrapper(true);
		setFullRowComparator(TestTableRowDataComparator.DEFAULT);
	}
}
