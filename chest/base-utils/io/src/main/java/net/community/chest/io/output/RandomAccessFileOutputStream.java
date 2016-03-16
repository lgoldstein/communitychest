/*
 *
 */
package net.community.chest.io.output;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import net.community.chest.io.IOAccessEmbedder;
import net.community.chest.io.IOPositionTracker;
import net.community.chest.io.OptionallyCloseable;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 1, 2009 2:56:45 PM
 */
public class RandomAccessFileOutputStream extends OutputStream
    implements OptionallyCloseable, IOAccessEmbedder<RandomAccessFile>, IOPositionTracker {
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

    private RandomAccessFile    _rf;
    /*
     * @see net.community.chest.io.IOAccessEmbedder#getEmbeddedAccess()
     */
    @Override
    public RandomAccessFile getEmbeddedAccess ()
    {
        return _rf;
    }
    /*
     * @see net.community.chest.io.IOAccessEmbedder#setEmbeddedAccess(java.lang.Object)
     */
    @Override
    public void setEmbeddedAccess (RandomAccessFile c) throws IOException
    {
        _rf = c;
    }
    /**
     * Start position to read from random access file - default=0
     */
    private long    _startPos    /* =0L    */;
    public long getStartOffset ()
    {
        return _startPos;
    }

    public void setStartOffset (long o)
    {
        _startPos = o;
    }

    public RandomAccessFileOutputStream (RandomAccessFile rf, long startPos, boolean realClose)
    {
        _rf = rf;
        _startPos = startPos;
        _realClosure = realClose;
    }

    public RandomAccessFileOutputStream (RandomAccessFile rf, boolean realClose)
    {
        this(rf, 0L, realClose);
    }
    // NOTE: auto-closes the embedded accessor by default if set later
    public RandomAccessFileOutputStream (RandomAccessFile rf)
    {
        this(rf, true);
    }

    private boolean    _closed    /* =false */;
    public boolean isClosed ()
    {
        return _closed;
    }
    @Override
    public void close () throws IOException
    {
        if (!isClosed())
        {
            try
            {
                if (isRealClosure())
                {
                    Closeable    rf=getEmbeddedAccess();
                    if (rf != null) {
                        rf.close();
                    }
                }
            }
            finally
            {
                _closed = true;
            }
        }
    }

    private long    _curPos    /* =0L */;
    /*
     * @see net.community.chest.io.IOPositionTracker#getPos()
     */
    @Override
    public long getPos ()
    {
        return _curPos;
    }

    public void seek (long n) throws IOException
    {
        @SuppressWarnings("resource")
        final RandomAccessFile    rf=isClosed() ? null : getEmbeddedAccess();
        if (null == rf)
            throw new EOFException("No file available");

        if (n < 0L)
            throw new IOException("seek(" + n + ") negative values not allowed");

        rf.seek(n);
        _curPos = n;
    }
    /*
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush () throws IOException
    {
        @SuppressWarnings("resource")
        final RandomAccessFile    rf=isClosed() ? null : getEmbeddedAccess();
        if (null == rf)
            throw new EOFException("No file available");
    }
    /*
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write (byte[] b, int off, int len) throws IOException
    {
        @SuppressWarnings("resource")
        final RandomAccessFile    rf=isClosed() ? null : getEmbeddedAccess();
        if (null == rf)
            throw new EOFException("No file available");

        final long    startPos=Math.max(0L, getStartOffset()),
                    curPos=Math.max(0L, getPos()),
                    curOffset=curPos + startPos;
        rf.seek(curOffset);    // makes sure we are at the right offset
        rf.write(b, off, len);

        if (len > 0)
            _curPos += len;
    }
    /*
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write (byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }
    /*
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write (int b) throws IOException
    {
        write(new byte[] { (byte) (b & 0x00FF) });
    }

}
