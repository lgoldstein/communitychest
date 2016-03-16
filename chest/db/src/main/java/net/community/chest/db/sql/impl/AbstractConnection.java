/*
 *
 */
package net.community.chest.db.sql.impl;

import java.sql.ClientInfoStatus;
import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides some default implementations for {@link Connection} interface</P>
 *
 * @author Lyor G.
 * @since Feb 11, 2009 11:24:44 AM
 */
public abstract class AbstractConnection implements Connection {
    protected AbstractConnection ()
    {
        super();
    }

    private boolean    _autoCommit;
    /*
     * @see java.sql.Connection#getAutoCommit()
     */
    @Override
    public boolean getAutoCommit () throws SQLException
    {
        return _autoCommit;
    }
    /*
     * @see java.sql.Connection#setAutoCommit(boolean)
     */
    @Override
    public void setAutoCommit (boolean autoCommit) throws SQLException
    {
        _autoCommit = autoCommit;
    }

    private String    _catalog;
    /*
     * @see java.sql.Connection#getCatalog()
     */
    @Override
    public String getCatalog () throws SQLException
    {
        return _catalog;
    }
    /*
     * @see java.sql.Connection#setCatalog(java.lang.String)
     */
    @Override
    public void setCatalog (String catalog) throws SQLException
    {
        _catalog = catalog;
    }

    private Properties    _clientInfo;
    /*
     * @see java.sql.Connection#getClientInfo()
     */
    @Override
    public Properties getClientInfo () throws SQLException
    {
        return _clientInfo;
    }
    /*
     * @see java.sql.Connection#setClientInfo(java.util.Properties)
     */
    @Override
    public void setClientInfo (Properties properties) throws SQLClientInfoException
    {
        _clientInfo = properties;
    }
    /*
     * @see java.sql.Connection#getClientInfo(java.lang.String)
     */
    @Override
    public String getClientInfo (String name) throws SQLException
    {
        if ((null == name) || (name.length() <= 0))
            return null;

        final Properties    ci=getClientInfo();
        if (null == ci)
            return null;

        return ci.getProperty(name);
    }
    /*
     * @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String)
     */
    @Override
    public void setClientInfo (String name, String value)
            throws SQLClientInfoException
    {
        if ((null == name) || (name.length() <= 0))
        {
            final Map<String, ClientInfoStatus>    pm=new HashMap<String,ClientInfoStatus>(2, 1.0f);
            pm.put(String.valueOf(name), ClientInfoStatus.REASON_UNKNOWN);
            throw new SQLClientInfoException(pm);
        }

        try
        {
            Properties    ci=getClientInfo();
            if (null == ci)
            {
                ci = new Properties();
                setClientInfo(ci);
            }

            if ((value != null) && (value.length() > 0))
                ci.put(name, value);
            else
                ci.remove(name);
        }
        catch(SQLException e)
        {
            final Map<String, ClientInfoStatus>    pm=new HashMap<String,ClientInfoStatus>(2, 1.0f);
            pm.put(name, ClientInfoStatus.REASON_VALUE_INVALID);
            throw new SQLClientInfoException(pm);
        }
    }

    private int    _holdability=(-1);
    /*
     * @see java.sql.Connection#getHoldability()
     */
    @Override
    public int getHoldability () throws SQLException
    {
        return _holdability;
    }
    /*
     * @see java.sql.Connection#setHoldability(int)
     */
    @Override
    public void setHoldability (int holdability) throws SQLException
    {
        _holdability = holdability;
    }

    private int    _xactIsolation=(-1);
    /*
     * @see java.sql.Connection#getTransactionIsolation()
     */
    @Override
    public int getTransactionIsolation () throws SQLException
    {
        return _xactIsolation;
    }
    /*
     * @see java.sql.Connection#setTransactionIsolation(int)
     */
    @Override
    public void setTransactionIsolation (int level) throws SQLException
    {
        _xactIsolation = level;
    }

    private Map<String,Class<?>>    _typeMap;
    /*
     * @see java.sql.Connection#getTypeMap()
     */
    @Override
    public Map<String,Class<?>> getTypeMap () throws SQLException
    {
        return _typeMap;
    }
    /*
     * @see java.sql.Connection#setTypeMap(java.util.Map)
     */
    @Override
    public void setTypeMap (Map<String,Class<?>> map) throws SQLException
    {
        _typeMap = map;
    }

    private boolean    _readOnly;
    /*
     * @see java.sql.Connection#isReadOnly()
     */
    @Override
    public boolean isReadOnly () throws SQLException
    {
        return _readOnly;
    }
    /*
     * @see java.sql.Connection#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly (boolean readOnly) throws SQLException
    {
        _readOnly = readOnly;
    }
}
