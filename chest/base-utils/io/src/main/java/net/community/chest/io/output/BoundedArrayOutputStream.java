package net.community.chest.io.output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides the capability to build an output stream on an existing array, or an auto-allocated one,
 * and at the same time limit the buffer so it does not automatically grow - any attempt to write data beyond
 * the size limit causes an {@link ArrayIndexOutOfBoundsException} to be thrown</P>

 * @author Lyor G.
 * @since Aug 22, 2007 9:19:23 AM
 */
public class BoundedArrayOutputStream extends ByteArrayOutputStream {
    protected final int   _startOffset /* =0 */, _maxLen;
    protected int _remLen /* =0 */;
    /**
     * @return offset in underlying buffer where data has been written (if any)
     * @see #BoundedArrayOutputStream(byte[] buf, int startOffset, int maxLen)
     */
    public int getStartOffset ()
    {
        return _startOffset;
    }
    /**
     * Constructs an output stream and uses the supplied buffer for output accumulation
     * @param wbuf buffer to be used to hold output data - any attempt to write data beyond
     * the array size limit causes I/O exception to be thrown. Note: if a NULL/empty buffer is supplied,
     * then ANY attempt to write will cause an exception.
     * @param startOffset offset in supplied buffer to start writing
     * @param maxLen maximum allowed number of bytes
     */
    public BoundedArrayOutputStream (byte[] wbuf, int startOffset, int maxLen)
    {
        if ((null == (this.buf=wbuf))
         || ((_startOffset=startOffset) < 0)
         || ((_maxLen=maxLen) < 0)
         || ((startOffset+maxLen) > wbuf.length))
            throw new IllegalArgumentException("Bad/Illegal bounded buffer specification");

        this.count = 0;    // just making sure
        _remLen = maxLen;
    }
    /**
     * Constructs an output stream and uses the supplied buffer for output accumulation
     * @param wbuf buffer to be used to hold output data - any attempt to write data beyond
     * the array size limit causes I/O exception to be thrown. Note: if a NULL/empty buffer is supplied,
     * then ANY attempt to write will cause an exception.
     */
    public BoundedArrayOutputStream (byte[] wbuf)
    {
        this(wbuf, 0, (null == wbuf) ? 0 : wbuf.length);
    }
    /**
     * Constructs an output stream and uses an auto-allocated buffer of the specified size for output accumulation
     * @param maxLen size of buffer to be allocated for output - any attempt to write data beyond
     * the array size limit causes I/O exception to be thrown. Note: if a zero length buffer is specified,
     * then ANY attempt to write will cause an exception.
     */
    public BoundedArrayOutputStream (int maxLen)
    {
        this((maxLen <= 0) ? null : new byte[maxLen]);
    }
    /* NOTE !!! returns the internal buffer (CAVEAT EMPTOR) - this can be the
     * original buffer or the allocated one - depending on the constructor
     * that was originally used.
     * @see java.io.ByteArrayOutputStream#toByteArray()
     */
    @Override
    public synchronized byte[] toByteArray ()
    {
        return this.buf;
    }
    /* NOTE !!! throws ArrayIndexOutOfBoundsException if underlying write error
     * @see java.io.ByteArrayOutputStream#write(byte[], int, int)
     */
    @Override
    public synchronized void write (byte[] b, int off, int len)
    {
        if ((off < 0) || (len < 0) || (len > _remLen))
            throw new ArrayIndexOutOfBoundsException("Bad/Illegal offset/len to write");
        if (0 == len)
            return;
        if ((null == this.buf) || ((off + len) > this.buf.length))
            throw new ArrayIndexOutOfBoundsException("Bad/Illegal buffer range to write");

        System.arraycopy(b, off, this.buf, _startOffset + this.count, len);
        this.count += len;
        _remLen -= len;
    }
    /*
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write (byte[] b) throws IOException
    {
        try
        {
            write(b, 0, b.length);
        }
        catch(RuntimeException e)
        {
            throw new IOException(e.getClass().getName() + ": " + e.getMessage());
        }
    }
    /* NOTE !!! throws ArrayIndexOutOfBoundsException if underlying write error
     * @see java.io.ByteArrayOutputStream#write(int)
     */
    @Override
    public synchronized void write (int b)
    {
        write(new byte[] { (byte) b }, 0, 1);
    }
    /*
     * @see java.io.ByteArrayOutputStream#writeTo(java.io.OutputStream)
     */
    @Override
    public synchronized void writeTo (OutputStream out) throws IOException
    {
        if (null == out)
            throw new IOException("No output stream to write to");
        if (this.count > 0)
            out.write(this.buf, _startOffset, this.count);
    }

    @Override
    public synchronized void reset() {
        super.reset();
        _remLen = _maxLen;
    }
}
