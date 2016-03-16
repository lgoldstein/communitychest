package net.community.chest.rrd4j.common.core;

import java.io.IOException;
import java.net.BindException;

import org.rrd4j.core.RrdBackend;
import org.rrd4j.core.RrdBackendFactory;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 9, 2008 1:52:08 PM
 */
public class RrdBackendFactoryEmbedder extends RrdBackendFactoryExt {
    private RrdBackendFactory    _fac    /* =null */;
    public RrdBackendFactory getEmbeddedFactory ()
    {
        return _fac;
    }

    public void setEmbeddedFactory (RrdBackendFactory fac)
    {
        _fac = fac;
    }
    /**
     * @param fac The actual {@link RrdBackendFactory} to use. May be null
     * and/or substituted by a call to {@link #setEmbeddedFactory(RrdBackendFactory)}
     */
    public RrdBackendFactoryEmbedder (RrdBackendFactory fac)
    {
        _fac = fac;
    }

    public RrdBackendFactoryEmbedder ()
    {
        this(null);
    }
    /*
     * @see org.rrd4j.core.RrdBackendFactory#exists(java.lang.String)
     */
    @Override
    protected boolean exists (String path) throws IOException
    {
        final RrdBackendFactory    fac=getEmbeddedFactory();
        if (null == fac)
            throw new BindException("exists(" + path + ") no current factory instance");

        try
        {
            return RrdBackendFactoryExt.exists(fac, path);
        }
        catch(Exception e)
        {
            if (e instanceof IOException)
                throw (IOException) e;

            throw new BindException("exists(" + path + ") " + e.getClass().getName() + " while invoking embedded method: " + e.getMessage());
        }
    }
    /*
     * @see org.rrd4j.core.RrdBackendFactory#getFactoryName()
     */
    @Override
    public String getFactoryName ()
    {
        final RrdBackendFactory    fac=getEmbeddedFactory();
        if (null == fac)
            return null;
        else
            return fac.getFactoryName();
    }
    /*
     * @see org.rrd4j.core.RrdBackendFactory#open(java.lang.String, boolean)
     */
    @Override
    protected RrdBackend open (String path, boolean readOnly) throws IOException
    {
        final RrdBackendFactory    fac=getEmbeddedFactory();
        if (null == fac)
            throw new BindException("open(" + path + ")[read-only=" + readOnly + "] no current factory instance");

        try
        {
            return RrdBackendFactoryExt.open(fac, path, readOnly);
        }
        catch(Exception e)
        {
            if (e instanceof IOException)
                throw (IOException) e;

            throw new BindException("open(" + path + ")[read-only=" + readOnly + "] " + e.getClass().getName() + " while invoking embedded method: " + e.getMessage());
        }
    }
    /*
     * @see org.rrd4j.core.RrdBackendFactory#shouldValidateHeader(java.lang.String)
     */
    @Override
    protected boolean shouldValidateHeader (String path) throws IOException
    {
        final RrdBackendFactory    fac=getEmbeddedFactory();
        if (null == fac)
            throw new BindException("shouldValidateHeader(" + path + ") no current factory instance");

        try
        {
            return RrdBackendFactoryExt.shouldValidateHeader(fac, path);
        }
        catch(Exception e)
        {
            if (e instanceof IOException)
                throw (IOException) e;

            throw new BindException("shouldValidateHeader(" + path + ") " + e.getClass().getName() + " while invoking embedded method: " + e.getMessage());
        }
    }
}
