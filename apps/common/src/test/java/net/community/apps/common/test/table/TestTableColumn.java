/*
 * 
 */
package net.community.apps.common.test.table;

import net.community.chest.ui.helpers.table.EnumTableColumn;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 22, 2009 8:49:56 AM
 */
public class TestTableColumn extends EnumTableColumn<TestTableColumnType> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2934257292278713043L;

	public TestTableColumn (TestTableColumnType colIndex)
	{
		super(TestTableColumnType.class, colIndex);
	}
}
