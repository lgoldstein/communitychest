package net.community.chest.io.output;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>This class allows for finer control over the growth of the byte
 * array backing the output stream. At the same time, it overrides
 * the <I>toByteArray()</I> method by returning the actual backing
 * array rather than a <U>copy</I> of it (Caveat Emptor)</P>
 *
 * @author Lyor G.
 * @since Oct 10, 2007 11:52:56 AM
 */
public class AutoGrowArrayOutputStream extends ByteArrayOutputStream {
    protected final int    _growSize;
    /**
     * Resizes the array by adding enough bytes (if necessary) to accommodate
     * the specified required size (if already have enough room, then
     * nothing is done
     * @param reqSize required size - may not be zero/negative
     * @throws IllegalArgumentException  if zero/negative resize
     * @throws IllegalStateException if corrupted internal state
     */
    protected void resize (int reqSize)
        throws IllegalArgumentException, IllegalStateException
    {
        if (reqSize <= 0)
            throw new IllegalArgumentException("resize(" + reqSize + ") zero/negative resizing");

        final int    maxLen=(null == this.buf) ? 0 : this.buf.length;
        if (this.count < 0)
            throw new IllegalStateException("resize(" + reqSize + ") illegal current size: " + this.count);

        final int    availLen=maxLen - this.count;
        if (availLen >= reqSize)
            return;

        if (_growSize < 0)
            throw new IllegalStateException("resize(" + reqSize + ") illegal growth size: " + _growSize);

        final int        newLen=this.count + reqSize + _growSize;
        final byte[]    newBuf=new byte[newLen];
        if (this.count > 0)    // preserve the old data
            System.arraycopy(this.buf, 0, newBuf, 0, this.count);
        this.buf = newBuf;    // replace the current buffer
    }
    /**
     * Constructor
     * @param initialSize initial size - may be zero (but not negative)
     * @param growSize growth size when required - may be zero (but not negative)
     * @throws IllegalArgumentException if bad parameters
     */
    public AutoGrowArrayOutputStream (int initialSize, int growSize) throws IllegalArgumentException
    {
        super(initialSize);

        if (((_growSize=growSize) < 0) || (initialSize < 0))
            throw new IllegalArgumentException("AutoGrowArrayOutputStream(" + initialSize + "," + growSize +") bad/illegal values");
    }
    /**
     * Default constructor - the array will grow EXACTLY according to
     * required data size and no more - i.e., at every stage the returned
     * buffer's <I>length</I> attribute will equals the value returned by the
     * <I>size()</I> method
     * @see AutoGrowArrayOutputStream#AutoGrowArrayOutputStream(int, int)
     */
    public AutoGrowArrayOutputStream ()
    {
        this(0, 0);
    }
    /*
     * @see java.io.ByteArrayOutputStream#reset()
     */
    @Override
    public synchronized void reset ()
    {
        this.count = 0;
    }
    /**
     * @return actual backing array rather than a copy of it.
     * <B>Caveat Emptor:</B> call it <U><B>only</B></U> after
     * finished writing - otherwise, the actual array reference
     * may change due to resizing !!!
     * @see java.io.ByteArrayOutputStream#toByteArray()
     */
    @Override
    public synchronized byte[] toByteArray ()
    {
        return this.buf;
    }
    /*
     * @see java.io.ByteArrayOutputStream#size()
     */
    @Override
    public synchronized int size ()
    {
        return this.count;    // just making sure
    }
    /*
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public synchronized void write (byte[] b, int off, int len)
    {
        if ((off < 0) || (len < 0))
            throw new IllegalArgumentException("write(" + off + "," + len +") bad/illegal offset/length");
        if (0 == len)
            return;
        if ((null == b) || ((off + len) > b.length))
            throw new IllegalArgumentException("write(" + off + "," + len +") total elements exceed max available: " + ((null == b) ? 0 : b.length));

        resize(len);    // make sure have enough room
        System.arraycopy(b, off, this.buf, this.count, len);
        this.count += len;
    }
    /*
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public synchronized void write (int b)
    {
        resize(1);    // make sure have enough room
        this.buf[this.count] = (byte) b;
        this.count++;
    }
    /*
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write (byte[] b) throws IOException
    {
        write(b, 0, (null == b) ? 0 : b.length);
    }

    public static final Map.Entry<byte[],Integer> readAllData (final InputStream in, final AutoGrowArrayOutputStream    out) throws IOException
    {
        final long    rSize=IOCopier.copyStreams(in, out);
        if (rSize < 0L)
            throw new IOException("Failed (" + rSize + ") to read from input");

        final byte[]    data=out.toByteArray();
        final int    dLen=out.size();
        if (dLen != rSize)
            throw new IOException("Mismatched read (" + rSize + ") and data (" + dLen + ") sizes");

        return new MapEntryImpl<byte[],Integer>(data,Integer.valueOf(dLen));
    }

    public static final Map.Entry<byte[],Integer> readAllData (final InputStream in) throws IOException
    {
        return readAllData(in, new AutoGrowArrayOutputStream(0, IOCopier.DEFAULT_COPY_SIZE));
    }

    public static final Map.Entry<byte[],Integer> readAllData (final File f) throws IOException
    {
        final long    l=(null == f) ? (-1L) : f.length();
        if ((l < 0) || (l >= Integer.MAX_VALUE))
            throw new IOException("read(" + f + ") bad size: " + l);

        InputStream    in=null;
        try
        {
            in = new FileInputStream(f);
            return readAllData(in, new AutoGrowArrayOutputStream((int) l + Byte.MAX_VALUE, Byte.MAX_VALUE));
        }
        finally
        {
            FileUtil.closeAll(in);
        }
    }

    public static final Map.Entry<byte[],Integer> readAllData (final String filePath) throws IOException
    {
        return readAllData(new File(filePath));
    }

    public static final Map.Entry<byte[],Integer> readAllData (final URL url) throws IOException
    {
        InputStream    in=null;
        try
        {
            in = url.openStream();
            return readAllData(in);
        }
        finally
        {
            FileUtil.closeAll(in);
        }
    }
}
