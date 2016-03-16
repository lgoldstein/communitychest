package net.community.chest.io.output;

import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channel;

import net.community.chest.io.IOAccessEmbedder;
import net.community.chest.io.OptionallyCloseable;

/**
 * Copyright 2007 as per GPLv2
 *
 * Embeds an {@link OutputStream} while also implementing the
 * {@link OptionallyCloseable} interface
 *
 * @author Lyor G.
 * @since Jun 13, 2007 4:34:25 PM
 */
public class OutputStreamEmbedder extends FilterOutputStream
        implements OptionallyCloseable, IOAccessEmbedder<OutputStream>, Channel {
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
    public OutputStream getEmbeddedAccess ()
    {
        return this.out;
    }
    /*
     * @see net.community.chest.io.IOAccessEmbedder#setEmbeddedAccess(java.io.Closeable)
     */
    @Override
    public void setEmbeddedAccess (OutputStream c) throws IOException
    {
        this.out = c;
    }
    /*
     * @see java.nio.channels.Channel#isOpen()
     */
    @Override
    public boolean isOpen ()
    {
        return (getEmbeddedAccess() != null);
    }

    public OutputStreamEmbedder (OutputStream outStream, boolean realClosure)
    {
        super(outStream);

        _realClosure = realClosure;
    }
    /*
     * @see java.io.FilterOutputStream#write(byte[], int, int)
     */
    @Override
    public void write (byte[] b, int off, int len) throws IOException
    {
        if (!isOpen())
            throw new IOException("write(" + off + "/" + len + ") not open");
        if (null == this.out)
            throw new IOException("write(" + off + "/" + len + ") real stream already closed");
        this.out.write(b, off, len);
    }
    /*
     * @see java.io.FilterOutputStream#write(byte[])
     */
    @Override
    public void write (byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }
    /*
     * @see java.io.FilterOutputStream#write(int)
     */
    @Override
    public void write (int val) throws IOException
    {
        if (!isOpen())
            throw new IOException("write(" + val + ") not open");
        if (null == this.out)
            throw new IOException("write(" + val + ") real stream already closed");
        this.out.write(val);
    }
    /*
     * @see java.io.FilterOutputStream#flush()
     */
    @Override
    public void flush () throws IOException
    {
        if (!isOpen())
            throw new IOException("flush() not open");
        if (null == this.out)
            throw new IOException("No stream to flush");

        super.flush();
    }
    /*
     * @see java.io.FilterOutputStream#close()
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
