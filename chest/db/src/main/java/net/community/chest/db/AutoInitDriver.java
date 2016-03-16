/*
 *
 */
package net.community.chest.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import net.community.chest.db.sql.impl.AbstractDriver;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Uses the URL to determine the actual {@link Driver} class to load.
 * Expected format is &quot;auto:FQCN:...rest of URL...&quot;</P>
 *
 * @author Lyor G.
 * @since Oct 6, 2009 8:38:03 AM
 */
public class AutoInitDriver extends AbstractDriver {
    public AutoInitDriver ()
    {
        super();
    }
    /**
     * Default JDBC URL protocol used to indicate an auto-initialized driver
     */
    public static final String    AUTO_DRIVER_PROTOCOL="auto";
    /**
     * @param url JDBC URL protocol used to indicate an auto-initialized
     * driver - format is &quot;auto:FQCN:...rest of URL...&quot;
     * @return A pair represented as a {@link java.util.Map.Entry} whose key=the FQCN
     * of the {@link Driver} class to be instantiated, value=the effective
     * URL to be passed to it on subsequent calls. Returns <code>null</code>
     * if URL does not encode an auto-initialized driver JDBC URL
     * @see #AUTO_DRIVER_PROTOCOL
     */
    public static final Map.Entry<String,String> extractEncodedDriver (final String url)
    {
        final List<String>    comps=StringUtil.splitString(url, DriverUtils.URL_FRAGMENT_SEPARATOR_CHAR);
        final int            numComps=(null == comps) ? 0 : comps.size();
        if (numComps <= 2)    // must be at least "auto:class:...more..."
            return null;

        // check protocol
        final String    proto=comps.get(0);
        if (!AUTO_DRIVER_PROTOCOL.equalsIgnoreCase(proto))
            return null;

        // get class FQCN
        final String    dc=comps.get(1);
        if ((null == dc) || (dc.length() <= 0))
            return null;

        if (numComps == 2)
            return new MapEntryImpl<String,String>(dc, null);
        if (numComps == 3)
            return new MapEntryImpl<String,String>(dc, comps.get(2));

        // extract the effective URL
        final String    u=
            url.substring(proto.length() + 1 + dc.length());
        return new MapEntryImpl<String,String>(dc, u);
    }
    /**
     * <P>Called to determine if a given URL encodes an auto-init driver
     * class. The default calls {@link #extractEncodedDriver(String)} method.</P>
     * <P><B>Note:</B> if overriding this method then should also override
     * the {@link #createDriver(String)} should also be overridden to conform
     * to the new format
     * @param url The URL to be considered
     * @return TRUE if the url encodes an auto-init driver
     * @throws SQLException if cannot parse the URL correctly
     */
    public boolean isAutoInitURL (final String url) throws SQLException
    {
        final Map.Entry<String,String>    dp=extractEncodedDriver(url);
        final String                    dc=(null == dp) ? null : dp.getKey();
        if ((null == dc) || (dc.length() <= 0))
            return false;

        return true;
    }

    private Driver    _driver;
    public Driver getEncodedDriver ()
    {
        return _driver;
    }

    public void setEncodedDriver (Driver d)
    {
        _driver = d;
    }

    @Override
    public Logger getParentLogger () throws SQLFeatureNotSupportedException
    {
        Driver    driver=getEncodedDriver();
        if (driver == null)
            return null;
        else
            return driver.getParentLogger();
    }

    protected Map.Entry<Driver,String> createDriver (final String url) throws SQLException
    {
        final Map.Entry<String,String>    dp=extractEncodedDriver(url);
        final String                    dc=(null == dp) ? null : dp.getKey();
        if ((null == dc) || (dc.length() <= 0))
            throw new SQLException("createDriver(" + url + ") cannot extract encoded class");

        Driver    d=getEncodedDriver();
        if (null == d)
        {
            try
            {
                @SuppressWarnings("unchecked")
                final Class<? extends Driver>    cc=
                    (Class<? extends Driver>) ClassUtil.loadClassByName(dc);
                if (null == (d=cc.newInstance()))
                    throw new IllegalStateException("cannot create driver instance");
            }
            catch(Exception e)
            {
                if (e instanceof SQLException)
                    throw (SQLException) e;
                else
                    throw new SQLException("createDriver(" + url + ") " + e.getClass().getName() + ": " + e.getMessage());
            }

            setEncodedDriver(d);
        }
        else
        {
            final Class<?>    cc=d.getClass();
            final String    cn=(null == cc) ? null : cc.getName();
            if (StringUtil.compareDataStrings(cn, dc, false) != 0)
                throw new SQLException("createDriver(" + url + ") mismatched encoded drivers - got=" + dc + "/expected=" + cn);
        }

        return new MapEntryImpl<Driver,String>(d, dp.getValue());
    }
    /*
     * @see java.sql.Driver#acceptsURL(java.lang.String)
     */
    @Override
    public boolean acceptsURL (final String url) throws SQLException
    {
        return isAutoInitURL(url);
    }
    /*
     * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
     */
    @Override
    public Connection connect (final String url, final Properties info) throws SQLException
    {
        if (!isAutoInitURL(url))
            return null;    // see Javadoc for this method

        final Map.Entry<? extends Driver,String>    dp=createDriver(url);
        final Driver                                d=(null == dp) ? null : dp.getKey();
        final String                                u=(null == dp) ? null : dp.getValue();
        if (null == d)
            throw new SQLException("connect(" + url + ") no driver available");

        return d.connect(u, info);
    }
    /*
     * @see java.sql.Driver#getPropertyInfo(java.lang.String, java.util.Properties)
     */
    @Override
    public DriverPropertyInfo[] getPropertyInfo (final String url, final Properties info)
            throws SQLException
    {
        if (!isAutoInitURL(url))
            return null;

        final Map.Entry<? extends Driver,String>    dp=createDriver(url);
        final Driver                                d=(null == dp) ? null : dp.getKey();
        final String                                u=(null == dp) ? null : dp.getValue();
        if (null == d)
            throw new SQLException("getPropertyInfo(" + url + ") no driver available");

        return d.getPropertyInfo(u, info);
    }
}
