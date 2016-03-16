package net.community.chest.io.output;

import java.io.IOException;
import java.io.OutputStream;

import net.community.chest.io.IOPositionTracker;

/**
 * Copyright 2007 as per GPLv2
 *
 * @author Lyor G.
 * @since Jul 12, 2007 5:32:42 PM
 */
public class TrackingOutputStream extends OutputStreamEmbedder implements IOPositionTracker {
    private long    _curPos    /* =0L */;
    /*
     * @see net.community.chest.io.IOPositionTracker#getPos()
     */
    @Override
    public long getPos ()
    {
        return _curPos;
    }

    public TrackingOutputStream (OutputStream outStream, boolean realClosure)
    {
        super(outStream, realClosure);
    }
    /*
     * @see java.io.FilterOutputStream#write(byte[], int, int)
     */
    @Override
    public void write (byte[] b, int off, int len) throws IOException
    {
        super.write(b, off, len);
        _curPos += len;
    }
    /*
     * @see java.io.FilterOutputStream#write(int)
     */
    @Override
    public void write (int b) throws IOException
    {
        super.write(b);
        _curPos++;
    }
}
