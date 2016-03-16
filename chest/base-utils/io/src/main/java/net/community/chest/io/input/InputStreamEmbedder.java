package net.community.chest.io.input;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channel;

import net.community.chest.io.IOAccessEmbedder;
import net.community.chest.io.OptionallyCloseable;

/**
 * Copyright 2007 as per GPLv2
 *
 * Embeds an {@link InputStream} while also implementing the
 * {@link OptionallyCloseable} interface
 *
 * @author Lyor G.
 * @since Jun 14, 2007 8:11:30 AM
 */
public class InputStreamEmbedder extends FilterInputStream
        implements OptionallyCloseable, IOAccessEmbedder<InputStream>, Channel {
    /*
     * @see net.community.chest.io.OptionallyCloseable#isMutableRealClosure()
     */
    @Override
    public boolean isMutableRealClosure ()
    {
        return true;
    }

    private boolean    _realClosure;
    /*
     * @see net.community.chest.io.OptionallyCloseable#isRealClosure()
     */
    @Override
    public boolean isRealClosure ()
    {
        return _realClosure;
    }
    /*
     * @see net.community.chest.io.OptionallyCloseable#setRealClosure(boolean)
     */
    @Override
    public void setRealClosure (boolean enabled) throws UnsupportedOperationException
    {
        _realClosure = enabled;
    }
    /*
     * @see net.community.chest.io.IOAccessEmbedder#getEmbeddedAccess()
     */
    @Override
    public InputStream getEmbeddedAccess ()
    {
        return this.in;
    }
    /*
     * @see net.community.chest.io.IOAccessEmbedder#setEmbeddedAccess(java.io.Closeable)
     */
    @Override
    public void setEmbeddedAccess (InputStream c) throws IOException
    {
        this.in = c;
    }

    public InputStreamEmbedder (InputStream inStream, boolean realClosure)
    {
        super(inStream);

        _realClosure = realClosure;
    }
    /*
     * @see java.nio.channels.Channel#isOpen()
     */
    @Override
    public boolean isOpen ()
    {
        return getEmbeddedAccess() != null;
    }
    /*
     * @see java.io.FilterInputStream#read(byte[], int, int)
     */
    @Override
    public int read (byte[] b, int off, int len) throws IOException
    {
        if (!isOpen())
            throw new IOException("read(" + off + "/" + len + ") not open");
        return super.read(b, off, len);
    }
    /*
     * @see java.io.FilterInputStream#read(byte[])
     */
    @Override
    public int read (byte[] b) throws IOException
    {
        if (!isOpen())
            throw new IOException("read([]) not open");

        return read(b, 0, b.length);
    }
    /*
     * @see java.io.FilterInputStream#close()
     */
    @Override
    public void close () throws IOException
    {
        final Closeable    s=getEmbeddedAccess();
        if (s != null)
        {
            try
            {
                if (isRealClosure())
                    s.close();
            }
            finally
            {
                setEmbeddedAccess(null);
            }
        }
    }
}
