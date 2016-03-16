/*
 *
 */
package net.community.chest.db.sql.impl;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;


/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Provide some default implementation for some {@link PreparedStatement} methods
 * @author Lyor G.
 * @since May 19, 2011 10:52:25 AM
 */
public abstract class AbstractPreparedStatement extends AbstractStatement implements PreparedStatement {
    private final Map<Integer,Object>    _indexedParams=new TreeMap<Integer,Object>();
    protected Map<Integer,Object> getIndexedParametersMap ()
    {
        return _indexedParams;
    }

    protected <K,T> T getMappedParameter (final Map<? extends K,?> valuesMap, final K key, Class<T> paramType, boolean mustExist) throws SQLException
    {
        final Object    value=valuesMap.get(key);
        if (value == null)
        {
            if (mustExist)
                throw new SQLException("getMappedParameter(" + key + ") no value available");
            return null;
        }

        final Class<?>    vc=value.getClass();
        if (paramType.isAssignableFrom(vc))
            return paramType.cast(value);

        throw new SQLException("getMappedParameter(" + key + ") value type (" + vc.getName() + ") not compatible with expected (" + paramType.getName() + ")");
    }

    protected <T> T getIndexedParameter (int parameterIndex, Class<T> paramType, boolean mustExist) throws SQLException
    {
        checkIndexedParameterAccess("getIndexedParameter", parameterIndex);
        return getMappedParameter(getIndexedParametersMap(), Integer.valueOf(parameterIndex), paramType, mustExist);
    }
    /*
     * @see java.sql.PreparedStatement#setBoolean(int, boolean)
     */
    @Override
    public void setBoolean (int parameterIndex, boolean x) throws SQLException
    {
        addIndexedParameter(parameterIndex, Boolean.valueOf(x));
    }
    /*
     * @see java.sql.PreparedStatement#setByte(int, byte)
     */
    @Override
    public void setByte (int parameterIndex, byte x) throws SQLException
    {
        addIndexedParameter(parameterIndex, Byte.valueOf(x));
    }
    /*
     * @see java.sql.PreparedStatement#setShort(int, short)
     */
    @Override
    public void setShort (int parameterIndex, short x) throws SQLException
    {
        addIndexedParameter(parameterIndex, Short.valueOf(x));
    }
    /*
     * @see java.sql.PreparedStatement#setInt(int, int)
     */
    @Override
    public void setInt (int parameterIndex, int x) throws SQLException
    {
        addIndexedParameter(parameterIndex, Integer.valueOf(x));
    }
    /*
     * @see java.sql.PreparedStatement#setLong(int, long)
     */
    @Override
    public void setLong (int parameterIndex, long x) throws SQLException
    {
        addIndexedParameter(parameterIndex, Long.valueOf(x));
    }
    /*
     * @see java.sql.PreparedStatement#setFloat(int, float)
     */
    @Override
    public void setFloat (int parameterIndex, float x) throws SQLException
    {
        addIndexedParameter(parameterIndex, Float.valueOf(x));
    }
    /*
     * @see java.sql.PreparedStatement#setDouble(int, double)
     */
    @Override
    public void setDouble (int parameterIndex, double x) throws SQLException
    {
        addIndexedParameter(parameterIndex, Double.valueOf(x));
    }
    /*
     * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
     */
    @Override
    public void setBigDecimal (int parameterIndex, BigDecimal x) throws SQLException
    {
        addIndexedParameter(parameterIndex, x);
    }
    /*
     * @see java.sql.PreparedStatement#setString(int, java.lang.String)
     */
    @Override
    public void setString (int parameterIndex, String x) throws SQLException
    {
        addIndexedParameter(parameterIndex, x);
    }
    /*
     * @see java.sql.PreparedStatement#setBytes(int, byte[])
     */
    @Override
    public void setBytes (int parameterIndex, byte[] x) throws SQLException
    {
        addIndexedParameter(parameterIndex, x);
    }
    /*
     * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
     */
    @Override
    public void setDate (int parameterIndex, Date x) throws SQLException
    {
        addIndexedParameter(parameterIndex, x);
    }
    /*
     * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
     */
    @Override
    public void setTime (int parameterIndex, Time x) throws SQLException
    {
        addIndexedParameter(parameterIndex, x);
    }
    /*
     * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
     */
    @Override
    public void setTimestamp (int parameterIndex, Timestamp x) throws SQLException
    {
        addIndexedParameter(parameterIndex, x);
    }
    /*
     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
     */
    @Override
    public void setObject (int parameterIndex, Object x) throws SQLException
    {
        addIndexedParameter(parameterIndex, x);
    }
    /*
     * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
     */
    @Override
    public void setRef (int parameterIndex, Ref x) throws SQLException
    {
        addIndexedParameter(parameterIndex, x);
    }
    /*
     * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
     */
    @Override
    public void setBlob (int parameterIndex, Blob x) throws SQLException
    {
        addIndexedParameter(parameterIndex, x);
    }
    /*
     * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
     */
    @Override
    public void setClob (int parameterIndex, Clob x) throws SQLException
    {
        addIndexedParameter(parameterIndex, x);
    }
    /*
     * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
     */
    @Override
    public void setArray (int parameterIndex, Array x) throws SQLException
    {
        addIndexedParameter(parameterIndex, x);
    }
    /*
     * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
     */
    @Override
    public void setURL (int parameterIndex, URL x) throws SQLException
    {
        addIndexedParameter(parameterIndex, x);
    }
    /*
     * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
     */
    @Override
    public void setNClob (int parameterIndex, NClob value) throws SQLException
    {
        addIndexedParameter(parameterIndex, value);
    }
    /*
     * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
     */
    @Override
    public void setSQLXML (int parameterIndex, SQLXML xmlObject) throws SQLException
    {
        addIndexedParameter(parameterIndex, xmlObject);
    }
    /**
     * Adds a parameter to the {@link PreparedStatement}
     * @param parameterIndex The index - must be &gt;0
     * @param value The set value - cannot be <code>null</code>
     * @return The previously mapped value for the index - <code>null</code> if none
     * @throws SQLException If closed statement, non-positive index or
     * <code>null</code> value
     */
    protected Object addIndexedParameter (int parameterIndex, Object value) throws SQLException
    {
        checkIndexedParameterAccess("addIndexedParameter", parameterIndex);
        if (value == null)    // leave handling of NULL values to the implementor
            throw new SQLException("addIndexedParameter(" + parameterIndex + ") NULL values N/A");

        final Map<Integer,Object>    valuesMap=getIndexedParametersMap();
        return valuesMap.put(Integer.valueOf(parameterIndex), value);
    }

    protected void checkIndexedParameterAccess (String location, int parameterIndex) throws SQLException
    {
        final String    actualLocation=location + "(" + parameterIndex + ")";

        checkClosedStatement(actualLocation);
        if (parameterIndex <= 0)    // must start at 1
            throw new SQLException(actualLocation + " - bad index");
    }

    protected AbstractPreparedStatement ()
    {
        super(true);
    }
}
