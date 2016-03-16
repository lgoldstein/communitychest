package net.community.chest.io.output;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.Writer;
import java.nio.channels.Channel;

/**
 * Copyright 2007 as per GPLv2
 *
 * A {@link Writer} that ignores anything written to it
 *
 * @author Lyor G.
 * @since Jul 12, 2007 4:30:23 PM
 */
public class NullWriter extends Writer implements Channel {
    public NullWriter ()
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
     * @see java.io.Writer#close()
     */
    @Override
    public void close () throws IOException
    {
        if (!isClosed())
            setClosed(true);
    }
    /*
     * @see java.io.Writer#flush()
     */
    @Override
    public void flush () throws IOException
    {
        if (isClosed())
            throw new EOFException("flush() closed");
    }
    /*
     * @see java.io.Writer#write(char[], int, int)
     */
    @Override
    public void write (char[] cbuf, int off, int len) throws IOException
    {
        if (isClosed())
            throw new EOFException("write(" + off + "-" + (off + len) + ") closed");
        if ((len < 0) || (off < 0) || ((off + len) > cbuf.length))
            throw new StreamCorruptedException("write(" + off + "-" + (off + len) + ") bad buffer");
    }
    /*
     * @see java.io.Writer#append(char)
     */
    @Override
    public Writer append (char c) throws IOException
    {
        if (isClosed())
            throw new EOFException("append(" + c + ") closed");
        return this;
    }
    /*
     * @see java.io.Writer#append(java.lang.CharSequence, int, int)
     */
    @Override
    public Writer append (CharSequence csq, int start, int end) throws IOException
    {
        if (isClosed())
            throw new EOFException("append(" + start + "-" + end + ") closed");
        if ((start < 0) || (end < 0) || (start > end) || (start >= csq.length()) || (end >= csq.length()))
            throw new StreamCorruptedException("append(" + start + "-" + end + ") bad sequence");
        return this;
    }
    /*
     * @see java.io.Writer#append(java.lang.CharSequence)
     */
    @Override
    public Writer append (CharSequence csq) throws IOException
    {
        return append(csq, 0, csq.length());
    }
    /*
     * @see java.io.Writer#write(char[])
     */
    @Override
    public void write (char[] cbuf) throws IOException
    {
        write(cbuf, 0, cbuf.length);
    }
    /*
     * @see java.io.Writer#write(int)
     */
    @Override
    public void write (int c) throws IOException
    {
        if (isClosed())
            throw new EOFException("write(" + c + ") closed");
    }
    /*
     * @see java.io.Writer#write(java.lang.String, int, int)
     */
    @Override
    public void write (String str, int off, int len) throws IOException
    {
        append(str, off, len);
    }
    /*
     * @see java.io.Writer#write(java.lang.String)
     */
    @Override
    public void write (String str) throws IOException
    {
        write(str, 0, str.length());
    }
}
