/*
 *
 */
package net.community.chest.db.sql.impl;

import java.sql.Driver;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Provides some useful default functionality for {@link Driver}
 * implementations</P>
 *
 * @author Lyor G.
 * @since Oct 6, 2009 8:32:33 AM
 */
public abstract class AbstractDriver implements Driver {
    protected AbstractDriver ()
    {
        super();
    }

    private int    _majorVersion;
    /*
     * @see java.sql.Driver#getMajorVersion()
     */
    @Override
    public int getMajorVersion ()
    {
        return _majorVersion;
    }

    public void setMajorVersion (int v)
    {
        _majorVersion = v;
    }

    private int    _minorVersion;
    /*
     * @see java.sql.Driver#getMinorVersion()
     */
    @Override
    public int getMinorVersion ()
    {
        return _minorVersion;
    }

    public void setMinorVersion (int v)
    {
        _minorVersion = v;
    }

    private boolean    _jbdcCompliant=true;
    public boolean isJbdcCompliant ()
    {
        return _jbdcCompliant;
    }

    public void setJbdcCompliant (boolean jbdcCompliant)
    {
        _jbdcCompliant = jbdcCompliant;
    }
    /*
     * @see java.sql.Driver#jdbcCompliant()
     */
    @Override
    public boolean jdbcCompliant ()
    {
        return isJbdcCompliant();
    }
}
