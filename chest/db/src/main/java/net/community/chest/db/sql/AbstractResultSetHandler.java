package net.community.chest.db.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 30, 2008 1:27:37 PM
 */
public abstract class AbstractResultSetHandler {
    protected AbstractResultSetHandler ()
    {
        super();
    }
    /**
     * Called by {@link #processResultSetRows(ResultSet)} for each recovered row data
     * @param rowIndex The current row index - starts at 1, 2, 3...
     * @param cl The data of the columns - column 1 data is at position 0, 2
     * at position 1, etc. - represented as {@link ResultSetRowColumnData} instances
     * @return TRUE=keep processing the next row
     * @throws SQLException If cannot process the data
     */
    public abstract boolean processResultRow (int rowIndex, List<? extends ResultSetRowColumnData>    cl) throws SQLException;
    /**
     * Processes the currently set {@link ResultSet} by invoking the
     * (abstract) {@link #processResultRow(int, List)} method for each
     * recovered row data
     * @param rs The {@link ResultSet} to use (the last set one is ignored)
     * @return Number of processed rows - till all processed or FALSE
     * returned by {@link #processResultRow(int, List)} method invocation
     * @throws SQLException If failures encountered during row processing
     */
    public int processResultSetRows (final ResultSet rs) throws SQLException
    {
        int    numRows=1;
        for (final ResultSetRowDataIterator    iter=(null == rs) ? null : new ResultSetRowDataIterator(rs);
             (iter != null) && iter.hasNext();
             numRows++)
        {
            final List<? extends ResultSetRowColumnData>    cl=iter.next();
            if (!processResultRow(numRows, cl))
                break;
        }

        return numRows - 1;
    }
}
