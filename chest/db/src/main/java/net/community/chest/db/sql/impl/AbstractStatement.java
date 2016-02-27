/*
 * 
 */
package net.community.chest.db.sql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.community.chest.db.sql.ResultSetFetchDirection;

/**
 * <P>Copyright as per GPLv2</P>
 * 
 * <P>Provide some default implementations for {@link Statement} interface
 * @author Lyor G.
 * @since May 19, 2011 10:25:19 AM
 */
public abstract class AbstractStatement implements Statement {
	protected AbstractStatement (boolean defaultPoolable)
	{
		_poolable = defaultPoolable;
	}

	private int	_fetchDirection=ResultSet.FETCH_FORWARD;
	/*
	 * @see java.sql.Statement#getFetchDirection()
	 */
	@Override
	public int getFetchDirection () throws SQLException
	{
		checkClosedStatement("getFetchDirection()");
		return _fetchDirection;
	}
	/*
	 * @see java.sql.Statement#setFetchDirection(int)
	 */
	@Override
	public void setFetchDirection (int direction) throws SQLException
	{
		final ResultSetFetchDirection	d=ResultSetFetchDirection.fromDirection(direction);
		checkClosedStatement("setDirection(" + direction + ")[" + d + "]");
		if (d == null)
			throw new SQLException("setDirection(" + direction + ") unknown value");

		_fetchDirection = direction;
	}

	private int	_fetchSize;
	/*
	 * @see java.sql.Statement#getFetchSize()
	 */
	@Override
	public int getFetchSize () throws SQLException
	{
		checkClosedStatement("getFetchSize()");
		return _fetchSize;
	}
	/*
	 * @see java.sql.Statement#setFetchSize(int)
	 */
	@Override
	public void setFetchSize (int rows) throws SQLException
	{
		checkClosedStatement("setFetchSize(" + rows + ")");
		if (rows < 0)
			throw new SQLException("setFetchSize(" + rows + ") illegal value");

		_fetchSize = rows;
	}

	private int	_maxFieldSize;
	/*
	 * @see java.sql.Statement#getMaxFieldSize()
	 */
	@Override
	public int getMaxFieldSize () throws SQLException
	{
		checkClosedStatement("getMaxFieldSize()");
		return _maxFieldSize;
	}
	/*
	 * @see java.sql.Statement#setMaxFieldSize(int)
	 */
	@Override
	public void setMaxFieldSize (int max) throws SQLException
	{
		checkClosedStatement("setMaxFieldSize(" + max + ")");
		if (max < 0)
			throw new SQLException("setMaxFieldSize(" + max + ") illegal value");

		_maxFieldSize = max;
	}

	private int	_maxRows;
	/*
	 * @see java.sql.Statement#getMaxRows()
	 */
	@Override
	public int getMaxRows () throws SQLException
	{
		checkClosedStatement("getMaxRows()");
		return _maxRows;
	}
	/*
	 * @see java.sql.Statement#setMaxRows(int)
	 */
	@Override
	public void setMaxRows (int max) throws SQLException
	{
		checkClosedStatement("setMaxRows(" + max + ")");
		if (max < 0)
			throw new SQLException("setMaxRows(" + max + ") illegal value");

		_maxRows = max;
	}

	private int	_queryTimeout;
	/*
	 * @see java.sql.Statement#getQueryTimeout()
	 */
	@Override
	public int getQueryTimeout () throws SQLException
	{
		checkClosedStatement("getQueryTimeout()");
		return _queryTimeout;
	}
	/*
	 * @see java.sql.Statement#setQueryTimeout(int)
	 */
	@Override
	public void setQueryTimeout (int seconds) throws SQLException
	{
		checkClosedStatement("setQueryTimeout(" + seconds + ")");
		if (seconds < 0)
			throw new SQLException("setQueryTimeout(" + seconds + ") illegal value");

		_queryTimeout = seconds;
	}

	private boolean	_poolable;
	/*
	 * @see java.sql.Statement#isPoolable()
	 */
	@Override
	public boolean isPoolable () throws SQLException
	{
		checkClosedStatement("isPoolable");
		return _poolable;
	}
	/*
	 * @see java.sql.Statement#setPoolable(boolean)
	 */
	@Override
	public void setPoolable (boolean poolable) throws SQLException
	{
		checkClosedStatement("setPoolable(" + poolable + ")");
		_poolable = poolable;
	}
	/**
	 * Called by various default implementations to check if the {@link Statement#isClosed()}
	 * @param location An identifier of the caller used to generate the
	 * exception message
	 * @throws SQLException If statement is closed - the message is derived
	 * from the <I>location</I> parameter
	 */
	protected void checkClosedStatement (String location) throws SQLException
	{
		if (isClosed())
			throw new SQLException(location + " - closed statement");
	}
}
