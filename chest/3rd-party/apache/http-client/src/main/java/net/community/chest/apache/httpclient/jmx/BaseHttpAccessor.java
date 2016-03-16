package net.community.chest.apache.httpclient.jmx;

import java.io.IOException;
import java.net.BindException;
import java.net.URI;

import net.community.chest.net.proto.jmx.AbstractJMXAccessorHelper;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 10, 2008 9:40:51 AM
 */
public abstract class BaseHttpAccessor extends AbstractJMXAccessorHelper {
    protected BaseHttpAccessor ()
    {
        super();
    }

    private HostConfiguration    _host    /* =null */;
    public synchronized HostConfiguration getHostConfiguration ()
    {
        if (null == _host)
        {
            final URI    u=getAccessURL();
            if (null == u)
                return null;

            _host = new HostConfiguration();
            _host.setHost(u.getHost(), u.getPort(), u.getScheme());
        }

        return _host;
    }
    // CAVEAT EMPTOR !!!
    public synchronized void setHostConfiguration (HostConfiguration host)
    {
        _host = host;
    }

    private HttpConnectionManager    _mgr    /* =null */;
    public synchronized HttpConnectionManager getConnectionManager ()
    {
        if (null == _mgr)
            _mgr = new MultiThreadedHttpConnectionManager();
        return _mgr;
    }
    // CAVEAT EMPTOR !!!
    public synchronized void setConnectionManager (HttpConnectionManager mgr)
    {
        _mgr = mgr;
    }

    public HttpClient getClientInstance () throws IOException
    {
        final HttpConnectionManager    mgr=getConnectionManager();
        final HttpClient            hc=(null == mgr) ? null : new HttpClient(mgr);
        if (null == hc)
            throw new BindException("No client instance created");

        return hc;
    }
}
