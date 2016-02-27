/*
 * 
 */
package net.community.apps.common.test.table;

import net.community.chest.ui.helpers.table.EnumColumnTypedTable;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 22, 2009 8:44:35 AM
 */
public class TestTable extends EnumColumnTypedTable<TestTableColumnType,TestTableRowData> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6436942345036710208L;
	public TestTable (TestTableModel m)
	{
		super(m);
	}

	public TestTable ()
	{
		this(new TestTableModel());
	}
	/*
	 * @see net.community.chest.ui.helpers.table.EnumColumnTypedTable#getTypedModel()
	 */
	@Override
	public TestTableModel getTypedModel ()
	{
		return TestTableModel.class.cast(super.getTypedModel());
	}
}
