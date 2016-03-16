package net.community.chest.io.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.nio.channels.Channel;

/**
 * Copyright 2007 as per GPLv2
 *
 * An "empty" {@link Reader} implementation that returns EOF on any attempt
 * to {@link #read()} something from it
 *
 * @author Lyor G.
 * @since Jul 12, 2007 4:23:16 PM
 */
public class NullReader extends Reader implements Channel {
    public NullReader ()
    {
        super();
    }

    private boolean    _isClosed    /* =false */;
    public boolean isClosed ()
    {
        return _isClosed;
    }
    // CAVEAT EMPTOR - you might re-open it after being closed
    public void setClosed (boolean isClosed)
    {
        _isClosed = isClosed;
    }
    /*
     * @see java.nio.channels.Channel#isOpen()
     */
    @Override
    public boolean isOpen ()
    {
        return !isClosed();
    }
    /*
     * @see java.io.Reader#close()
     */
    @Override
    public void close () throws IOException
    {
        if (!isClosed())
            setClosed(true);
    }
    /*
     * @see java.io.Reader#mark(int)
     */
    @Override
    public void mark (int readAheadLimit) throws IOException
    {
        if (isClosed())
            throw new EOFException("mark(" + readAheadLimit + ") closed");
    }
    /*
     * @see java.io.Reader#markSupported()
     */
    @Override
    public boolean markSupported ()
    {
        return false;
    }
    /*
     * @see java.io.Reader#read()
     */
    @Override
    public int read () throws IOException
    {
        if (isClosed())
            throw new EOFException("read() closed");
        return (-1);
    }
    /*
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read (char[] cbuf, int off, int len) throws IOException
    {
        if (isClosed())
            throw new EOFException("read(" + off + "-" + (off + len) + ") closed");
        if ((len < 0) || (off < 0) || ((off + len) > cbuf.length))
            throw new StreamCorruptedException("read(" + off + "-" + (off + len) + ") bad buffer");

        return (-1);
    }
    /*
     * @see java.io.Reader#read(char[])
     */
    @Override
    public int read (char[] cbuf) throws IOException
    {
        return read(cbuf, 0, cbuf.length);
    }
    /*
     * @see java.io.Reader#ready()
     */
    @Override
    public boolean ready () throws IOException
    {
        if (isClosed())
            throw new EOFException("ready() closed");
        return true;
    }
    /*
     * @see java.io.Reader#reset()
     */
    @Override
    public void reset () throws IOException
    {
        if (isClosed())
            throw new EOFException("reset() closed");
    }
    /*
     * @see java.io.Reader#skip(long)
     */
    @Override
    public long skip (long n) throws IOException
    {
        if (isClosed())
            throw new EOFException("skip(" + n + ") closed");
        return n;
    }
}
