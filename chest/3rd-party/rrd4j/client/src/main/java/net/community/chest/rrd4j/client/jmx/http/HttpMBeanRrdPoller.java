package net.community.chest.rrd4j.client.jmx.http;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.util.Collection;

import net.community.chest.apache.httpclient.jmx.JMXSession;
import net.community.chest.net.proto.jmx.JMXAccessor;
import net.community.chest.rrd4j.client.jmx.AbstractMBeanRrdPoller;
import net.community.chest.rrd4j.common.jmx.MBeanRrdDef;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 10, 2008 11:43:12 AM
 */
public class HttpMBeanRrdPoller extends AbstractMBeanRrdPoller {
    private URI    _url    /* =null */;
    public URI getAccessURL ()
    {
        return _url;
    }
    // can be set while thread is not running
    public void setAccessURL (URI u)
    {
        if (_url != null)
        {
            if (getThisThread() != null)
                throw new IllegalStateException("setAccessURL(" + u + ") not allowed while running");
        }

        if (null == (_url=u))
            throw new IllegalArgumentException("setAccessURL() no URL specifed");
    }

    public HttpMBeanRrdPoller (URI u, Collection<? extends MBeanRrdDef> defs) throws IllegalArgumentException, IllegalStateException
    {
        super(defs);
        _url = u;
    }

    public HttpMBeanRrdPoller (Collection<? extends MBeanRrdDef> defs) throws IllegalArgumentException, IllegalStateException
    {
        this(null, defs);
    }

    private JMXAccessor    _acc    /* =null */;
    /*
     * @see net.community.chest.rrd4j.client.jmx.AbstractMBeanRrdPoller#getJMXAccessor()
     */
    @Override
    public JMXAccessor getJMXAccessor () throws Exception
    {
        if (null == _acc)
        {
            final URI    u=getAccessURL();
            if (null == u)
                throw new ConnectException("getJMXAccessor() no current URL set");
            _acc = new JMXSession();
            _acc.connect(u);
        }

        return _acc;
    }
    /*
     * @see net.community.chest.rrd4j.client.jmx.AbstractMBeanRrdPoller#close()
     */
    @Override
    public void close () throws IOException
    {
        if (null == _acc)
            _acc = null;

        super.close();
    }
}
