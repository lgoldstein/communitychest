/*
 * 
 */
package net.community.chest.db.test;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;

import net.community.chest.db.sql.AbstractResultSetHandler;
import net.community.chest.db.sql.ResultSetRowColumnData;

public class SQLResultPrinter extends AbstractResultSetHandler {
	private final PrintStream	_out;
	public SQLResultPrinter (final PrintStream out)
	{
		_out = out;
	}
	/*
	 * @see net.community.chest.db.sql.AbstractResultSetHandler#processResultRow(int, java.util.List)
	 */
	@Override
	public boolean processResultRow (final int rowIndex, final List<? extends ResultSetRowColumnData> cl) throws SQLException
	{
		if ((null == cl) || (cl.size() <= 0))
			return false;

		for (int	idx=(rowIndex <= 1) ? (-1) : 0; idx < 1; idx++)
		{
			for (final ResultSetRowColumnData rsd : cl)
			{
				if (idx < 0) 
				{
					final String	/* tName=rsd.getTableName(), throws Exception for Sybase*/
									cName=rsd.getColumnName(),
									cType=rsd.getColumnTypeName();
					_out.print("\t" + /* tName + "." + */ cName + "[" + cType + "] null=" + rsd.isNullable());
				}
				else
				{
					final Object	colValue=rsd.getColumnValue();
					_out.print("\t" + colValue);
				}
			}

			_out.println();
		}

		return true;
	}
}