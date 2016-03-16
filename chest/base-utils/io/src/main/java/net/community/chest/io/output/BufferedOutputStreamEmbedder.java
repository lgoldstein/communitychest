package net.community.chest.io.output;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.community.chest.io.IOAccessEmbedder;
import net.community.chest.io.OptionallyCloseable;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 9:10:09 AM
 */
public abstract class BufferedOutputStreamEmbedder extends BufferedOutputStream
        implements OptionallyCloseable, IOAccessEmbedder<OutputStream> {
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
    /**
     * Base constructor
     * @param os "real" output stream into which decoded data is to be written
     * @param size internal buffering size - MUST be POSITIVE
     * @param realClose if true the call to {@link #close()} also closes
     * the underlying stream
     * @throws IllegalArgumentException if bad/illegal underlying stream/size
     */
    protected BufferedOutputStreamEmbedder (OutputStream os, int size, boolean realClose) throws IllegalArgumentException
    {
        super(os, size);

        if (null == this.out)
            throw new IllegalArgumentException("No output stream to embed");
        if (size <= 0)
            throw new IllegalArgumentException("Bad/illegal buffering size: " + size);

        setRealClosure(realClose);
    }
    /**
     * Base constructor
     * @param os "real" output stream into which decoded data is to be written
     * @param size internal buffering size - MUST be POSITIVE
     * @throws IllegalArgumentException if bad/illegal underlying stream/size
     * @see #setRealClosure(boolean)
     */
    protected BufferedOutputStreamEmbedder (OutputStream os, int size) throws IllegalArgumentException
    {
        this(os, size, true);
    }
    /*
     * @see java.io.FilterOutputStream#write(byte[])
     */
    @Override
    public void write (byte[] wbuf) throws IOException
    {
        write(wbuf, 0, (null == wbuf) ? 0 : wbuf.length);
    }
    /*
     * @see java.io.BufferedOutputStream#write(int)
     */
    @Override
    public synchronized void write (int val) throws IOException
    {
        write(new byte[] { (byte) val });
    }
    /*
     * @see java.io.FilterOutputStream#close()
     */
    @Override
    public void close () throws IOException
    {
        final OutputStream    s=getEmbeddedAccess();
        if (s != null)
        {
            try
            {
                s.flush();

                if (isRealClosure())
                    s.close();
            }
            finally
            {
                setEmbeddedAccess(null);
            }
        }
    }
    /*
     * @see java.io.BufferedOutputStream#flush()
     */
    @Override
    public synchronized void flush () throws IOException
    {
        if (null == this.out)
            throw new IOException("No stream to flush");

        super.flush();
    }
}
