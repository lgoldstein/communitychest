/*
 * 
 */
package net.community.apps.common.test.table;

import java.util.EnumMap;

import net.community.chest.util.datetime.TimeUnits;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 22, 2009 8:29:34 AM
 */
public class TestTableRowData extends EnumMap<TestTableColumnType,Object>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5113125375011187166L;
	private final long	_stamp;
	public final long getTimestamp ()
	{
		return _stamp;
	}

	public TestTableRowData (final int rIndex)
	{
		super(TestTableColumnType.class);

		_stamp = System.currentTimeMillis() + TimeUnits.MINUTE.getMilisecondValue(rIndex);
	}
}
