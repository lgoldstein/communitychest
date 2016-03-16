/*
 *
 */
package net.community.chest.io.encode.endian;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;

import net.community.chest.io.encode.OutputDataEncoder;
import net.community.chest.io.file.FileIOUtils;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 13, 2009 12:17:02 PM
 */
public abstract class AbstractEndianOutputEncoder
        implements OutputDataEncoder, ByteOrderControlled {
    private ByteOrder    _outOrder;
    /*
     * @see net.community.chest.io.encode.endian.ByteOrderControlled#getByteOrder()
     */
    @Override
    public ByteOrder getByteOrder ()
    {
        return _outOrder;
    }
    /*
     * @see net.community.chest.io.encode.endian.ByteOrderControlled#setByteOrder(java.nio.ByteOrder)
     */
    @Override
    public void setByteOrder (ByteOrder o)
    {
        _outOrder = o;
    }
    /*
     * @see net.community.chest.io.encode.endian.ByteOrderControlled#isMutableByteOrder()
     */
    @Override
    public boolean isMutableByteOrder ()
    {
        return true;
    }

    protected AbstractEndianOutputEncoder (final ByteOrder inOrder)
    {
        _outOrder = inOrder;
    }

    protected AbstractEndianOutputEncoder ()
    {
        this(null);
    }

    private byte[]    _workBuf;
    /**
     * @param reqSize Required size - if buffer size already at least the
     * required value then current work buffer is returned
     * @param createIfNotExist FALSE=do not create a buffer if none allocated
     * or existing one not up to required size
     * @return Work buffer instance
     * @see #setWorkBuf(byte[])
     */
    protected byte[] getWorkBuf (final int reqSize, final boolean createIfNotExist)
    {
        if (((null == _workBuf) || (_workBuf.length < reqSize)) && createIfNotExist)
            _workBuf = new byte[Math.max(reqSize, 8 /* at least a long value */)];

        return _workBuf;
    }
    // returns previous value
    protected byte[] setWorkBuf (byte[] buf)
    {
        final byte[]    prev=_workBuf;
        _workBuf = buf;
        return prev;
    }
    /*
     * @see java.io.DataOutput#write(byte[])
     */
    @Override
    public void write (byte[] b) throws IOException
    {
        write(b, 0, (null == b) ? 0 : b.length);
    }
    /*
     * @see java.io.DataOutput#write(int)
     */
    @Override
    public void write (int b) throws IOException
    {
        writeByte(b);

    }
    /*
     * @see java.io.DataOutput#writeInt(int)
     */
    @Override
    public void writeInt (final int v) throws IOException
    {
        final byte[]    workBuf=getWorkBuf(4, true);
        try
        {
            EndianEncoder.toInt32ByteArray(v, getByteOrder(), workBuf, 0);
            write(workBuf, 0, 4);
        }
        catch(NumberFormatException e)
        {
            throw new IOException("writeInt(" + v + ") " + e.getMessage());
        }
    }
    /*
     * @see java.io.DataOutput#writeLong(long)
     */
    @Override
    public void writeLong (final long v) throws IOException
    {
        final byte[]    workBuf=getWorkBuf(8, true);
        try
        {
            EndianEncoder.toInt64ByteArray(v, getByteOrder(), workBuf, 0);
            write(workBuf, 0, 8);
        }
        catch(NumberFormatException e)
        {
            throw new IOException("writeLong(" + v + ") " + e.getMessage());
        }
    }
    /*
     * @see java.io.DataOutput#writeShort(int)
     */
    @Override
    public void writeShort (final int v) throws IOException
    {
        final byte[]    workBuf=getWorkBuf(2, true);
        try
        {
            EndianEncoder.toInt16ByteArray(v, getByteOrder(), workBuf, 0);
            write(workBuf, 0, 2);
        }
        catch(NumberFormatException e)
        {
            throw new IOException("writeShort(" + v + ") " + e.getMessage());
        }
    }

    protected abstract void writeStringBytes (
            String s, CharsetEncoder charsetEnc, byte[] data, int off, int len)
        throws IOException;

    protected void writeStringBytes (
            String s, CharsetEncoder charsetEnc, byte ... data)
        throws IOException
    {
        writeStringBytes(s, charsetEnc, data, 0, (null == data) ? 0 : data.length);
    }
    /*
     * @see net.community.chest.io.encode.OutputDataEncoder#writeString(java.lang.String, java.nio.charset.CharsetEncoder)
     */
    @Override
    public void writeString (String s, CharsetEncoder orgEnc) throws IOException
    {
        final int    sLen=(null == s) ? 0 : s.length();
        if (sLen <= 0)
        {
            writeStringBytes(s, orgEnc, FileIOUtils.EMPTY_BYTES);
            return;
        }

        final CharBuffer    cb=CharBuffer.wrap(s);
        final float            fac=orgEnc.maxBytesPerChar(),
                            bLen=(sLen + 2 /* a little extra */) * fac;
        final byte[]        workBuf=getWorkBuf(Math.round(bLen), true);
        final ByteBuffer    bb=ByteBuffer.wrap(workBuf);
        Arrays.fill(workBuf, (byte) 0);    // TODO remove this code

        final CoderResult    crEnc, flEnc;
        synchronized(orgEnc)
        {
            final CharsetEncoder    enc=orgEnc.reset();
            crEnc = enc.encode(cb, bb, true);
            flEnc = enc.flush(bb);
        }

        if ((flEnc != CoderResult.UNDERFLOW) || (null == crEnc))
            throw new StreamCorruptedException("writeString(" + s + ")[" + orgEnc + "] bad encode result: " + flEnc);

        writeStringBytes(s, orgEnc, workBuf, 0, bb.position());
    }
    /*
     * @see net.community.chest.io.encode.OutputDataEncoder#writeString(java.lang.String, java.nio.charset.Charset)
     */
    @Override
    public void writeString (String s, Charset charset) throws IOException
    {
        writeString(s, charset.newEncoder());
    }
    /*
     * @see net.community.chest.io.encode.OutputDataEncoder#writeString(java.lang.String, java.lang.String)
     */
    @Override
    public void writeString (String s, String charsetName) throws IOException
    {
        writeString(s, Charset.forName(charsetName));
    }
    /*
     * @see java.io.DataOutput#writeUTF(java.lang.String)
     */
    @Override
    public void writeUTF (String s) throws IOException
    {
        writeString(s, "UTF-8");
    }
    /*
     * @see java.io.DataOutput#writeBoolean(boolean)
     */
    @Override
    public void writeBoolean (boolean v) throws IOException
    {
        throw new StreamCorruptedException("writeBoolean(" + v + ") N/A");
    }
    /*
     * @see java.io.DataOutput#writeBytes(java.lang.String)
     */
    @Override
    public void writeBytes (String s) throws IOException
    {
        throw new StreamCorruptedException("writeBytes(" + s + ") N/A");
    }
    /*
     * @see java.io.DataOutput#writeChar(int)
     */
    @Override
    public void writeChar (int v) throws IOException
    {
        throw new StreamCorruptedException("writeChar(" + String.valueOf((char) v) + ") N/A");
    }
    /*
     * @see java.io.DataOutput#writeChars(java.lang.String)
     */
    @Override
    public void writeChars (String s) throws IOException
    {
        throw new StreamCorruptedException("writeChars(" + s + ") N/A");
    }
    /*
     * @see java.io.DataOutput#writeDouble(double)
     */
    @Override
    public void writeDouble (double v) throws IOException
    {
        throw new StreamCorruptedException("writeDouble(" + v + ") N/A");
    }
    /*
     * @see java.io.DataOutput#writeFloat(float)
     */
    @Override
    public void writeFloat (float v) throws IOException
    {
        throw new StreamCorruptedException("writeFloat(" + v + ") N/A");
    }
}
