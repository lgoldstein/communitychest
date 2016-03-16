/*
 *
 */
package net.community.chest.db.sql.impl;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.sql.CommonDataSource;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 14, 2009 9:20:16 AM
 */
public abstract class AbstractCommonDataSource implements CommonDataSource {
    protected AbstractCommonDataSource ()
    {
        super();
    }

    private int    _loginTimeout;
    /*
     * @see javax.sql.CommonDataSource#getLoginTimeout()
     */
    @Override
    public int getLoginTimeout () throws SQLException
    {
        return _loginTimeout;
    }
    /*
     * @see javax.sql.CommonDataSource#setLoginTimeout(int)
     */
    @Override
    public void setLoginTimeout (int seconds) throws SQLException
    {
        _loginTimeout = seconds;
    }

    private PrintWriter    _logWriter;
    /*
     * @see javax.sql.CommonDataSource#getLogWriter()
     */
    @Override
    public PrintWriter getLogWriter () throws SQLException
    {
        return _logWriter;
    }
    /*
     * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
     */
    @Override
    public void setLogWriter (PrintWriter out) throws SQLException
    {
        _logWriter = out;
    }
}
