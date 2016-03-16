package net.community.chest.db.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 30, 2008 2:04:42 PM
 */
public class ResultSetRowDataIterator implements Iterator<List<? extends ResultSetRowColumnData>> {
    private final ResultSet    _rs;
    public ResultSetRowDataIterator (ResultSet rs)
    {
        _rs = rs;
    }

    private List<? extends ResultSetRowColumnData>    _cl    /* =null */;
    private Boolean                                    _moreRows    /* =null */;
    /*
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext ()
    {
        if (null == _rs)
            return false;

        if (null == _cl)
        {
            try
            {
                final boolean    moreRows=(ResultSet.TYPE_FORWARD_ONLY == _rs.getType()) ? _rs.next() : _rs.first();
                if (moreRows)
                {
                    final ResultSetMetaData    md=_rs.getMetaData();
                    _cl = ResultSetRowColumnData.buildColumnsDataList(md);
                }

                return moreRows;
            }
            catch(SQLException e)
            {
                throw new NoSuchElementException("hasNext() " + e.getClass().getName() + "[" + e.getSQLState() + "](err=" + e.getErrorCode() + "): " + e.getMessage());
            }
        }

        return _moreRows.booleanValue();
    }
    /*
     * @see java.util.Iterator#next()
     */
    @Override
    public List<? extends ResultSetRowColumnData> next ()
    {
        if ((_moreRows != null) && (!_moreRows.booleanValue()))
            throw new NoSuchElementException("next() no more elements");

        try
        {
            for (final ResultSetRowColumnData rsd : _cl)
            {
                final int        cIndex=rsd.getColumnIndex();
                final Object    colValue=_rs.getObject(cIndex);
                rsd.setColumnValue(colValue);
            }

            _moreRows = Boolean.valueOf(_rs.next());
        }
        catch(SQLException e)
        {
            throw new NoSuchElementException("next() " + e.getClass().getName() + "[" + e.getSQLState() + "](err=" + e.getErrorCode() + "): " + e.getMessage());
        }

        return _cl;
    }
    /*
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove ()
    {
        throw new UnsupportedOperationException("remove() N/A");
    }
}
