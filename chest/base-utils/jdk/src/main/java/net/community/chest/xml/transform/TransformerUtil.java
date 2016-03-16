/*
 *
 */
package net.community.chest.xml.transform;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import net.community.chest.io.IOCopier;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since May 12, 2010 9:37:30 AM
 */
public final class TransformerUtil {
    private TransformerUtil ()
    {
        // no instance
    }
    /**
     * Attempts to extract some useful I/O input from a {@link StreamSource}
     * using the following <U>order</U> and moving to next in line if the
     * previous option yielded a <code>null</code></BR>
     * <UL>
     *         <LI>{@link StreamSource#getInputStream()}</LI>
     *         <LI>{@link StreamSource#getReader()}</LI>
     *         <LI>{@link StreamSource#getSystemId()} as a {@link URL}</LI>
     * </UL>
     * @param src The {@link StreamSource} instance
     * @param maxTimeout Timeout (sec.) to use in case need to open a URL
     * @return The {@link Closeable} instance - may be <code>null</code> if
     * no source set
     * @throws TransformerException If failed to open/use an available source
     */
    public static final Closeable resolveInput (
            final StreamSource src, final int maxTimeout)
        throws TransformerException
    {
        if (null == src)
            return null;

        // try the input stream first
        {
            final InputStream    in=src.getInputStream();
            if (in != null)
                return in;
        }

        // try the Reader second
        {
            final Reader    r=src.getReader();
            if (r != null)
                return r;
        }

        // try the system-id as an URL
        final String    sysId=src.getSystemId();
        if ((sysId != null) && (sysId.length() > 0))
        {
            try
            {
                return IOCopier.openURLForRead(sysId, maxTimeout);
            }
            catch(URISyntaxException e)
            {
                throw new TransformerException("resolveInput(URI=" + sysId + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
            }
            catch(IOException e)
            {
                throw new TransformerException("resolveInput(URI=" + sysId + ") " + e.getClass() + ": " + e.getMessage(), e);
            }
        }

        return null;
    }

    public static final InputSource resolveInputSource (final StreamSource src)
    {
        if (null == src)
            return null;
        // try the input stream first
        {
            final InputStream    in=src.getInputStream();
            if (in != null)
                return new InputSource(in);
        }

        // try the Reader second
        {
            final Reader    r=src.getReader();
            if (r != null)
                return new InputSource(r);
        }

        // try the system-id as an URL
        final String    sysId=src.getSystemId();
        if ((sysId != null) && (sysId.length() > 0))
            return new InputSource(sysId);

        return null;
    }
    /**
     * Attempts to extract some useful I/O output stream from the {@link StreamResult}
     * using the following <U>order</U> and moving to next in line if the
     * previous option yielded a <code>null</code></BR>
     * <UL>
     *         <LI>{@link StreamResult#getOutputStream()}</LI>
     *         <LI>{@link StreamResult#getWriter()}</LI>
     *         <LI>{@link StreamResult#getSystemId()} as a {@link URL}</LI>
     * </UL>
     * @param res The {@link StreamResult} instance - ignored if <code>null</code>
     * @param maxTimeout Timeout (sec.) to use in case need to open a URL
     * @return The {@link Closeable} instance - may be <code>null</code> if
     * no result set
     * @throws TransformerException If failed to open/use an available source
     */
    public static final Closeable resolveOutput (
            final StreamResult res, final int maxTimeout)
        throws TransformerException
    {
        if (null == res)
            return null;

        {
            final OutputStream    out=res.getOutputStream();
            if (out != null)
                return out;
        }

        {
            final Writer    w=res.getWriter();
            if (w != null)
                return w;
        }

        final String    sysId=res.getSystemId();
        if ((sysId != null) && (sysId.length() > 0))
        {
            try
            {
                return IOCopier.openURLForWrite(sysId, maxTimeout);
            }
            catch(URISyntaxException e)
            {
                throw new TransformerException("resolveOutput(URI=" + sysId + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
            }
            catch(IOException e)
            {
                throw new TransformerException("resolveOutput(URI=" + sysId + ") " + e.getClass() + ": " + e.getMessage(), e);
            }
        }

        return null;
    }

    public static final void transform (File in, Result out, Transformer t)
        throws TransformerException
    {
        if (null == t)
            throw new TransformerException("transform(" + in + ") no " + Transformer.class.getSimpleName() + " instance");

        t.transform((null == in) ? null : new StreamSource(in), out);
    }

    public static final void transform (InputStream in, Result out, Transformer t)
        throws TransformerException
    {
        if (null == t)
            throw new TransformerException("transform(" + in + ") no " + Transformer.class.getSimpleName() + " instance");

        t.transform((null == in) ? null : new StreamSource(in), out);
    }

    public static final void transform (Reader in, Result out, Transformer t)
        throws TransformerException
    {
        if (null == t)
            throw new TransformerException("transform(" + in + ") no " + Transformer.class.getSimpleName() + " instance");

        t.transform((null == in) ? null : new StreamSource(in), out);
    }

    public static final void transform (Source in, File out, Transformer t)
        throws TransformerException
    {
        if (null == t)
            throw new TransformerException("transform(" + out + ") no " + Transformer.class.getSimpleName() + " instance");

        t.transform(in, (null == out) ? null : new StreamResult(out));
    }

    public static final void transform (Source in, OutputStream out, Transformer t)
        throws TransformerException
    {
        if (null == t)
            throw new TransformerException("transform(" + out + ") no " + Transformer.class.getSimpleName() + " instance");

        t.transform(in, (null == out) ? null : new StreamResult(out));
    }

    public static final void transform (Source in, Writer out, Transformer t)
        throws TransformerException
    {
        if (null == t)
            throw new TransformerException("transform(" + out + ") no " + Transformer.class.getSimpleName() + " instance");

        t.transform(in, (null == out) ? null : new StreamResult(out));
    }
}
