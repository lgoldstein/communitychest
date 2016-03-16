package net.community.chest.io.input;

import java.io.IOException;
import java.io.InputStream;

import net.community.chest.io.IOPositionTracker;

/**
 * Copyright 2007 as per GPLv2
 *
 * @author Lyor G.
 * @since Jul 12, 2007 5:30:20 PM
 */
public class TrackingInputStream extends InputStreamEmbedder implements IOPositionTracker {
    private long    _curPos    /* =0L */;
    /*
     * @see net.community.chest.io.IOPositionTracker#getPos()
     */
    @Override
    public long getPos ()
    {
        return _curPos;
    }

    public TrackingInputStream (InputStream inStream, boolean realClosure)
    {
        super(inStream, realClosure);
    }

    public TrackingInputStream (InputStream inStream)
    {
        this(inStream, true);
    }
    /*
     * @see java.io.FilterInputStream#read()
     */
    @Override
    public int read () throws IOException
    {
        final int    ret=super.read();
        if (ret != (-1))
            _curPos++;
        return ret;
    }
    /*
     * @see java.io.FilterInputStream#read(byte[], int, int)
     */
    @Override
    public int read (byte[] b, int off, int len) throws IOException
    {
        final int    ret=super.read(b, off, len);
        if (ret > 0)
            _curPos += ret;
        return ret;
    }

    /*
     * @see java.io.FilterInputStream#reset()
     */
    @Override
    public synchronized void reset () throws IOException
    {
        super.reset();
        _curPos = 0L;
    }
    /*
     * @see java.io.FilterInputStream#skip(long)
     */
    @Override
    public long skip (long n) throws IOException
    {
        final long    ret=super.skip(n);
        if (ret > 0L)
            _curPos += ret;
        return ret;
    }
}
