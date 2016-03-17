/*
 *
 */
package net.community.apps.tools.adm;

import java.util.Map;

import javax.swing.AbstractButton;

import net.community.apps.common.BaseMainFrame;
import net.community.apps.common.resources.BaseAnchor;
import net.community.chest.db.DBAccessConfig;
import net.community.chest.db.DriverUtils;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @param <A> Type of {@link BaseAnchor} derived class
 * @author Lyor G.
 * @since Jun 23, 2010 1:41:11 PM
 */
public abstract class AbstractAccessMainFrame<A extends BaseAnchor> extends BaseMainFrame<A> {
    protected AbstractAccessMainFrame (boolean autoInit, String... args)
            throws Exception
    {
        super(autoInit, args);
    }

    protected AbstractAccessMainFrame (String... args) throws Exception
    {
        this(true, args);
    }

    public static final int        DEFAULT_PORT=1521;
    public static final char    HOST_FRAGMENT_SEPARATOR_CHAR='@';
    public static final String    DEFAULT_HOST="localhost",
                                DEFAULT_DRIVER_TYPE="thin",
                                DEFAULT_DB_TYPE="oracle",
                                HOST_FRAGMENT_SEPARATOR=String.valueOf(HOST_FRAGMENT_SEPARATOR_CHAR);

    public static final String buildOracleDefaultJDBCUrl (
            final String host, final int port, final String    sid)
        throws IllegalArgumentException
    {
        final String    hostVal=
            ((null == host) || (host.length() <= 0)) ? DEFAULT_HOST : host;
        final int        portVal=(port <= 0) ? DEFAULT_PORT : port,
                        sLen=(null == sid) ? 0 : sid.length();
        if (sLen <= 0)
            throw new IllegalArgumentException("buildOracleDefaultJDBCUrl(" + host + "@" + port + ") no SID specified");

        return DriverUtils.buildJDBCUrl(
                DriverUtils.DEFAULT_JDBC_URL_TYPE,
                DEFAULT_DB_TYPE,
                DEFAULT_DRIVER_TYPE,
                HOST_FRAGMENT_SEPARATOR + hostVal,
                String.valueOf(portVal),
                sid);
    }

    public static final String    DEFAULT_SID="nlayers";
    public static final String buildDefaultURL (final String    host,
                                                final int        port,
                                                final String    sid)
    {
        return buildOracleDefaultJDBCUrl(host, port, ((null == sid) || (sid.length() <= 0)) ? DEFAULT_SID : sid);
    }

    public static final String    NLAYERS_USER="nlayers", NLAYERS_PASSWORD="nlayers",
                                DEFAULT_URL=buildDefaultURL(null, 0, null);

    public static final DBAccessConfig fillDefaults (final DBAccessConfig cfg)
    {
        if (null == cfg)
            return null;

        cfg.setDriverClass(oracle.jdbc.driver.OracleDriver.class.getName());
        cfg.setURL(DEFAULT_URL);
        cfg.setUser(NLAYERS_USER);
        cfg.setPassword(NLAYERS_PASSWORD);
        return cfg;
    }

    public static final String updateURL (final DBAccessConfig cfg,
                                           final String            host,
                                           final String            port,
                                           final String            sid)
    {
        final String    hostVal=
            ((null == host) || (host.length() <= 0)) ? DEFAULT_HOST : host,
                        portVal=
            ((null == port) || (port.length() <= 0)) ? String.valueOf(DEFAULT_PORT) : port,
                        sidVal=
            ((null == sid) || (sid.length() <= 0)) ? DEFAULT_SID : sid,
                        urlVal=buildDefaultURL(hostVal, Integer.parseInt(portVal), sidVal);

        cfg.setURL(urlVal);
        return urlVal;
    }
    // returns TRUE if argument processed
    public static final boolean processDBAccessConfigParameter (
            final DBAccessConfig        cfg,
            final Map<String,String>    valsMap,
            final String                arg,
            final String                val)
    {
        if (null == cfg)
            return false;

        if ("-user".equalsIgnoreCase(arg))
            cfg.setUser(val);
        else if ("-password".equalsIgnoreCase(arg))
            cfg.setPassword(val);
        else if ("-driverClass".equalsIgnoreCase(arg))
            cfg.setDriverClass(val);
        else if ("-url".equalsIgnoreCase(arg))
            cfg.setURL(val);
        else if ("-host".equalsIgnoreCase(arg))
            updateURL(cfg,
                      val,
                      (null == valsMap) ? null : valsMap.get("-port"),
                      (null == valsMap) ? null : valsMap.get("-sid"));
        else if ("-port".equalsIgnoreCase(arg))
            updateURL(cfg,
                      (null == valsMap) ? null : valsMap.get("-host"),
                      val,
                      (null == valsMap) ? null : valsMap.get("-sid"));
        else if ("-sid".equalsIgnoreCase(arg))
            updateURL(cfg,
                      (null == valsMap) ? null : valsMap.get("-host"),
                      (null == valsMap) ? null : valsMap.get("-port"),
                       val);
        else
            return false;

        return true;
    }

    public static final void updateButtonsState (
            final boolean enabled, final AbstractButton ... btns)
    {
        if ((null == btns) || (btns.length <= 0))
            return;
        for (final AbstractButton b : btns)
        {
            if ((b != null) && (b.isEnabled() != enabled))
                b.setEnabled(enabled);
        }
    }
}
