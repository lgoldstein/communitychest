/*
 * 
 */
package net.community.chest.db.sql.impl;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
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
 * <P>Provide some default implementation for {@link CallableStatement}
 * @author Lyor G.
 * @since May 19, 2011 11:06:54 AM
 */
public abstract class AbstractCallableStatement extends AbstractPreparedStatement implements CallableStatement {
	/*
	 * @see java.sql.CallableStatement#getString(int)
	 */
	@Override
	public String getString (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, String.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getBoolean(int)
	 */
	@Override
	public boolean getBoolean (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Boolean.class, true).booleanValue();
	}
	/*
	 * @see java.sql.CallableStatement#getByte(int)
	 */
	@Override
	public byte getByte (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Byte.class, true).byteValue();
	}
	/*
	 * @see java.sql.CallableStatement#getShort(int)
	 */
	@Override
	public short getShort (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Short.class, true).shortValue();
	}
	/*
	 * @see java.sql.CallableStatement#getInt(int)
	 */
	@Override
	public int getInt (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Integer.class, true).intValue();
	}
	/*
	 * @see java.sql.CallableStatement#getLong(int)
	 */
	@Override
	public long getLong (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Long.class, true).longValue();
	}
	/*
	 * @see java.sql.CallableStatement#getFloat(int)
	 */
	@Override
	public float getFloat (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Float.class, true).floatValue();
	}
	/*
	 * @see java.sql.CallableStatement#getDouble(int)
	 */
	@Override
	public double getDouble (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Double.class, true).doubleValue();
	}
	/*
	 * @see java.sql.CallableStatement#getBytes(int)
	 */
	@Override
	public byte[] getBytes (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, byte[].class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getDate(int)
	 */
	@Override
	public Date getDate (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Date.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getTime(int)
	 */
	@Override
	public Time getTime (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Time.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getTimestamp(int)
	 */
	@Override
	public Timestamp getTimestamp (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Timestamp.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getObject(int)
	 */
	@Override
	public Object getObject (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Object.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getBigDecimal(int)
	 */
	@Override
	public BigDecimal getBigDecimal (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, BigDecimal.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getRef(int)
	 */
	@Override
	public Ref getRef (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Ref.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getBlob(int)
	 */
	@Override
	public Blob getBlob (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Blob.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getClob(int)
	 */
	@Override
	public Clob getClob (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Clob.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getArray(int)
	 */
	@Override
	public Array getArray (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, Array.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getURL(int)
	 */
	@Override
	public URL getURL (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, URL.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getNClob(int)
	 */
	@Override
	public NClob getNClob (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, NClob.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getSQLXML(int)
	 */
	@Override
	public SQLXML getSQLXML (int parameterIndex) throws SQLException
	{
		return getIndexedParameter(parameterIndex, SQLXML.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#setURL(java.lang.String, java.net.URL)
	 */
	@Override
	public void setURL (String parameterName, URL val) throws SQLException
	{
		addNamedParameter(parameterName, val);
	}
	/*
	 * @see java.sql.CallableStatement#setBoolean(java.lang.String, boolean)
	 */
	@Override
	public void setBoolean (String parameterName, boolean x) throws SQLException
	{
		addNamedParameter(parameterName, Boolean.valueOf(x));
	}
	/*
	 * @see java.sql.CallableStatement#setByte(java.lang.String, byte)
	 */
	@Override
	public void setByte (String parameterName, byte x) throws SQLException
	{
		addNamedParameter(parameterName, Byte.valueOf(x));
	}
	/*
	 * @see java.sql.CallableStatement#setShort(java.lang.String, short)
	 */
	@Override
	public void setShort (String parameterName, short x) throws SQLException
	{
		addNamedParameter(parameterName, Short.valueOf(x));
	}
	/*
	 * @see java.sql.CallableStatement#setInt(java.lang.String, int)
	 */
	@Override
	public void setInt (String parameterName, int x) throws SQLException
	{
		addNamedParameter(parameterName, Integer.valueOf(x));
	}
	/*
	 * @see java.sql.CallableStatement#setLong(java.lang.String, long)
	 */
	@Override
	public void setLong (String parameterName, long x) throws SQLException
	{
		addNamedParameter(parameterName, Long.valueOf(x));
	}
	/*
	 * @see java.sql.CallableStatement#setFloat(java.lang.String, float)
	 */
	@Override
	public void setFloat (String parameterName, float x) throws SQLException
	{
		addNamedParameter(parameterName, Float.valueOf(x));
	}
	/*
	 * @see java.sql.CallableStatement#setDouble(java.lang.String, double)
	 */
	@Override
	public void setDouble (String parameterName, double x) throws SQLException
	{
		addNamedParameter(parameterName, Double.valueOf(x));
	}
	/*
	 * @see java.sql.CallableStatement#setBigDecimal(java.lang.String, java.math.BigDecimal)
	 */
	@Override
	public void setBigDecimal (String parameterName, BigDecimal x) throws SQLException
	{
		addNamedParameter(parameterName, x);
	}
	/*
	 * @see java.sql.CallableStatement#setString(java.lang.String, java.lang.String)
	 */
	@Override
	public void setString (String parameterName, String x) throws SQLException
	{
		addNamedParameter(parameterName, x);
	}
	/*
	 * @see java.sql.CallableStatement#setBytes(java.lang.String, byte[])
	 */
	@Override
	public void setBytes (String parameterName, byte[] x) throws SQLException
	{
		addNamedParameter(parameterName, x);
	}
	/*
	 * @see java.sql.CallableStatement#setDate(java.lang.String, java.sql.Date)
	 */
	@Override
	public void setDate (String parameterName, Date x) throws SQLException
	{
		addNamedParameter(parameterName, x);
	}
	/*
	 * @see java.sql.CallableStatement#setTime(java.lang.String, java.sql.Time)
	 */
	@Override
	public void setTime (String parameterName, Time x) throws SQLException
	{
		addNamedParameter(parameterName, x);
	}
	/*
	 * @see java.sql.CallableStatement#setTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	@Override
	public void setTimestamp (String parameterName, Timestamp x) throws SQLException
	{
		addNamedParameter(parameterName, x);
	}
	/*
	 * @see java.sql.CallableStatement#setObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setObject (String parameterName, Object x) throws SQLException
	{
		addNamedParameter(parameterName, x);
	}
	/*
	 * @see java.sql.CallableStatement#getString(java.lang.String)
	 */
	@Override
	public String getString (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, String.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Boolean.class, true).booleanValue();
	}
	/*
	 * @see java.sql.CallableStatement#getByte(java.lang.String)
	 */
	@Override
	public byte getByte (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Byte.class, true).byteValue();
	}
	/*
	 * @see java.sql.CallableStatement#getShort(java.lang.String)
	 */
	@Override
	public short getShort (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Short.class, true).shortValue();
	}
	/*
	 * @see java.sql.CallableStatement#getInt(java.lang.String)
	 */
	@Override
	public int getInt (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Integer.class, true).intValue();
	}
	/*
	 * @see java.sql.CallableStatement#getLong(java.lang.String)
	 */
	@Override
	public long getLong (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Long.class, true).longValue();
	}
	/*
	 * @see java.sql.CallableStatement#getFloat(java.lang.String)
	 */
	@Override
	public float getFloat (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Float.class, true).floatValue();
	}
	/*
	 * @see java.sql.CallableStatement#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Double.class, true).doubleValue();
	}
	/*
	 * @see java.sql.CallableStatement#getBytes(java.lang.String)
	 */
	@Override
	public byte[] getBytes (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, byte[].class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getDate(java.lang.String)
	 */
	@Override
	public Date getDate (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Date.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getTime(java.lang.String)
	 */
	@Override
	public Time getTime (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Time.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getTimestamp(java.lang.String)
	 */
	@Override
	public Timestamp getTimestamp (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Timestamp.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getObject(java.lang.String)
	 */
	@Override
	public Object getObject (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Object.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getBigDecimal(java.lang.String)
	 */
	@Override
	public BigDecimal getBigDecimal (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, BigDecimal.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getRef(java.lang.String)
	 */
	@Override
	public Ref getRef (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Ref.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getBlob(java.lang.String)
	 */
	@Override
	public Blob getBlob (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Blob.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getClob(java.lang.String)
	 */
	@Override
	public Clob getClob (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Clob.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getArray(java.lang.String)
	 */
	@Override
	public Array getArray (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, Array.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#getURL(java.lang.String)
	 */
	@Override
	public URL getURL (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, URL.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#setNClob(java.lang.String, java.sql.NClob)
	 */
	@Override
	public void setNClob (String parameterName, NClob value) throws SQLException
	{
		addNamedParameter(parameterName, value);
	}
	/*
	 * @see java.sql.CallableStatement#getNClob(java.lang.String)
	 */
	@Override
	public NClob getNClob (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, NClob.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#setSQLXML(java.lang.String, java.sql.SQLXML)
	 */
	@Override
	public void setSQLXML (String parameterName, SQLXML xmlObject) throws SQLException
	{
		addNamedParameter(parameterName, xmlObject);
	}
	/*
	 * @see java.sql.CallableStatement#getSQLXML(java.lang.String)
	 */
	@Override
	public SQLXML getSQLXML (String parameterName) throws SQLException
	{
		return getNamedParameter(parameterName, SQLXML.class, true);
	}
	/*
	 * @see java.sql.CallableStatement#setBlob(java.lang.String, java.sql.Blob)
	 */
	@Override
	public void setBlob (String parameterName, Blob x) throws SQLException
	{
		addNamedParameter(parameterName, x);
	}
	/*
	 * @see java.sql.CallableStatement#setClob(java.lang.String, java.sql.Clob)
	 */
	@Override
	public void setClob (String parameterName, Clob x) throws SQLException
	{
		addNamedParameter(parameterName, x);
	}

	private final Map<String,Object>	_mappedParams;
	protected Map<String,Object> getMappedParameters ()
	{
		return _mappedParams;
	}

	protected <T> T getNamedParameter (String parameterName, Class<T> paramType, boolean mustExist) throws SQLException
	{
		checkNamedParameterAccess("getNamedParameter", parameterName);
		return getMappedParameter(getMappedParameters(), parameterName, paramType, mustExist);
	}
	/**
	 * Adds a parameter to the {@link CallableStatement}
	 * @param parameterName The parameter name - may not be <code>null</code>/empty
	 * @param value The set value - cannot be <code>null</code>
	 * @return The previously mapped value for the index - <code>null</code> if none
	 * @throws SQLException If closed statement, code>null</code>/empty name or
	 * <code>null</code> value 
	 */
	protected Object addNamedParameter (String parameterName, Object value) throws SQLException
	{
		checkNamedParameterAccess("addNamedParameter", parameterName);
		if (value == null)	// leave handling of NULL values to the implementor
			throw new SQLException("addNamedParameter(" + parameterName + ") NULL values N/A");

		final Map<String,Object>	valuesMap=getMappedParameters();
		return valuesMap.put(parameterName, value);
	}

	protected void checkNamedParameterAccess (String location, String parameterName) throws SQLException
	{
		final String	actualLocation=location + "(" + parameterName + ")";

		checkClosedStatement(actualLocation);
		if ((parameterName == null) || (parameterName.length() <= 0))
			throw new SQLException(actualLocation + " - bad name");
	}

	protected AbstractCallableStatement (boolean caseSensitive)
	{
		_mappedParams = caseSensitive
			? new TreeMap<String,Object>()
			: new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER)
			;
	}

}
