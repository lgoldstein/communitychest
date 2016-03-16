/*
 *
 */
package net.community.chest.io.encode.endian;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.ByteOrder;

import net.community.chest.io.file.FileIOUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 15, 2009 2:08:15 PM
 */
public final class EndianEncoder {
    private EndianEncoder ()
    {
        // no instance
    }

    public static final short readSignedInt16 (final ByteOrder inOrder, final byte[] data, final int off, final int len)
        throws NumberFormatException
    {
        if (null == inOrder)
            throw new NumberFormatException("readSignedInt16() no byte order specified");
        if (len < 2)
            throw new NumberFormatException("readSignedInt16(" + inOrder + ") insufficient data length: " + len);

        if (ByteOrder.BIG_ENDIAN.equals(inOrder))
            return (short) ((((data[off] << 8) & 0x00FF00) | (data[off + 1] & 0x00FF)) & 0x00FFFF);
        else
            return (short) ((((data[off + 1] << 8) & 0x00FF00) | (data[off] & 0x00FF)) & 0x00FFFF);
    }

    public static final short readSignedInt16 (final ByteOrder inOrder, final byte ... data)
        throws NumberFormatException
    {
        return readSignedInt16(inOrder, data, 0, (null == data) ? 0 : data.length);
    }

    public static final short readSignedInt16 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 2)
            throw new StreamCorruptedException("readSignedInt16(" + inOrder + ") insufficient data buffer size: " + len);

        FileIOUtils.readFully(inStream, workBuf, off, 2);
        return readSignedInt16(inOrder, workBuf, off, 2);
    }

    public static final short readSignedInt16 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf)
        throws IOException
    {
        return readSignedInt16(inStream, inOrder, workBuf, 0, (null == workBuf) ? 0 : workBuf.length);
    }

    public static final short readSignedInt16 (
            final InputStream inStream, final ByteOrder inOrder) throws IOException
    {
        final byte[]    data={ 0, 0 };
        return readSignedInt16(inStream, inOrder, data);
    }

    public static final int readUnsignedInt16 (final ByteOrder inOrder, final byte[] data, final int off, final int len)
        throws NumberFormatException
    {
        if (null == inOrder)
            throw new NumberFormatException("readUnsignedInt16() no byte order specified");
        if (len < 2)
            throw new NumberFormatException("readUnsignedInt16(" + inOrder + ") insufficient data length: " + len);

        if (ByteOrder.BIG_ENDIAN.equals(inOrder))
            return ((((data[off] << 8) & 0x00FF00) | (data[off + 1] & 0x00FF)) & 0x00FFFF);
        else
            return ((((data[off + 1] << 8) & 0x00FF00) | (data[off] & 0x00FF)) & 0x00FFFF);
    }

    public static final int readUnsignedInt16 (final ByteOrder inOrder, final byte ... data)
        throws NumberFormatException
    {
        return readUnsignedInt16(inOrder, data, 0, (null == data) ? 0 : data.length);
    }

    public static final int readUnsignedInt16 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 2)
            throw new StreamCorruptedException("readUnsignedInt16(" + inOrder + ") insufficient data buffer size: " + len);

        FileIOUtils.readFully(inStream, workBuf, off, 2);
        return readUnsignedInt16(inOrder, workBuf, off, 2);
    }

    public static final int readUnsignedInt16 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf)
        throws IOException
    {
        return readUnsignedInt16(inStream, inOrder, workBuf, 0, (null == workBuf) ? 0 : workBuf.length);
    }

    public static final int readUnsignedInt16 (
            final InputStream inStream, final ByteOrder inOrder) throws IOException
    {
        final byte[]    data={ 0, 0 };
        return readUnsignedInt16(inStream, inOrder, data);
    }

    public static final int readSignedInt32 (final ByteOrder inOrder, final byte[] data, final int off, final int len)
        throws NumberFormatException
    {
        if (null == inOrder)
            throw new NumberFormatException("readSignedInt32() no byte order specified");
        if (len < 4)
            throw new NumberFormatException("readSignedInt32(" + inOrder + ") insufficient data length: " + len);

        if (ByteOrder.BIG_ENDIAN.equals(inOrder))
            return (((data[off] << 24)      & 0x00FF000000)
                  | ((data[off+1] << 16) & 0x0000FF0000)
                  | ((data[off+2] << 8)  & 0x000000FF00)
                  | (data[off+3]          & 0x00000000FF))
                  ;
        else
            return (((data[off+3] << 24) & 0x00FF000000)
                  | ((data[off+2] << 16) & 0x0000FF0000)
                  | ((data[off+1] << 8)  & 0x000000FF00)
                  | (data[off]               & 0x00000000FF))
                  ;
    }

    public static final int readSignedInt32 (final ByteOrder inOrder, final byte ... data)
        throws NumberFormatException
    {
        return readSignedInt32(inOrder, data, 0, (null == data) ? 0 : data.length);
    }

    public static final int readSignedInt32 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 4)
            throw new StreamCorruptedException("readSignedInt32(" + inOrder + ") insufficient data buffer size: " + len);

        FileIOUtils.readFully(inStream, workBuf, off, 4);
        return readSignedInt32(inOrder, workBuf, off, 4);
    }

    public static final int readSignedInt32 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf)
        throws IOException
    {
        return readSignedInt32(inStream, inOrder, workBuf, 0, (null == workBuf) ? 0 : workBuf.length);
    }

    public static final int readSignedInt32 (
            final InputStream inStream, final ByteOrder inOrder) throws IOException
    {
        final byte[]    data={ 0, 0, 0, 0 };
        return readSignedInt32(inStream, inOrder, data);
    }

    public static final long readUnsignedInt32 (final ByteOrder inOrder, final byte[] data, final int off, final int len)
        throws NumberFormatException
    {
        if (null == inOrder)
            throw new NumberFormatException("readUnsignedInt32() no byte order specified");
        if (len < 4)
            throw new NumberFormatException("readUnsignedInt32(" + inOrder + ") insufficient data length: " + len);

        if (ByteOrder.BIG_ENDIAN.equals(inOrder))
            return (((data[off] << 24)   & 0x00FF000000L)
                  | ((data[off+1] << 16) & 0x0000FF0000L)
                  | ((data[off+2] << 8)  & 0x000000FF00L)
                  | (data[off+3]          & 0x00000000FFL))
                  ;
        else
            return (((data[off+3] << 24) & 0x00FF000000L)
                 | ((data[off+2] << 16)  & 0x0000FF0000L)
                 | ((data[off+1] << 8)   & 0x000000FF00L)
                 | (data[off]               & 0x00000000FFL))
                 ;
    }

    public static final long readUnsignedInt32 (final ByteOrder inOrder, final byte ... data)
        throws NumberFormatException
    {
        return readUnsignedInt32(inOrder, data, 0, (null == data) ? 0 : data.length);
    }

    public static final long readUnsignedInt32 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 4)
            throw new StreamCorruptedException("readUnsignedInt32(" + inOrder + ") insufficient data buffer size: " + len);

        FileIOUtils.readFully(inStream, workBuf, off, 4);
        return readUnsignedInt32(inOrder, workBuf, off, 4);
    }

    public static final long readUnsignedInt32 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf)
        throws IOException
    {
        return readUnsignedInt32(inStream, inOrder, workBuf, 0, (null == workBuf) ? 0 : workBuf.length);
    }

    public static final long readUnsignedInt32 (
            final InputStream inStream, final ByteOrder inOrder) throws IOException
    {
        final byte[]    data={ 0, 0, 0, 0 };
        return readUnsignedInt32(inStream, inOrder, data);
    }

    public static final long readSignedInt64 (final ByteOrder inOrder, final byte[] data, final int off, final int len)
        throws NumberFormatException
    {
        final int    maxIndex=off + len;
        if (null == inOrder)
            throw new NumberFormatException("readSignedInt64() no byte order specified");
        if (len < 8)
            throw new NumberFormatException("readSignedInt64(" + inOrder + ") insufficient data length: " + len);

        long    ret=0L;
        if (ByteOrder.BIG_ENDIAN.equals(inOrder))
        {
            for (int    dIndex=off, shiftSize=Long.SIZE - Byte.SIZE;
                 dIndex < maxIndex;
                 dIndex++, shiftSize -= Byte.SIZE)
            {
                long    val=data[dIndex] & 0x00FFL;
                if (shiftSize > 0)
                    val <<= shiftSize;
                ret |= val;
            }
        }
        else
        {
            for (int    dIndex=maxIndex, shiftSize=Long.SIZE - Byte.SIZE;
                 dIndex > off;
                 dIndex--, shiftSize -= Byte.SIZE)
            {
                long    val=data[dIndex-1] & 0x00FFL;
                if (shiftSize > 0)
                    val <<= shiftSize;
                ret |= val;
            }
        }

        return ret;
    }

    public static final long readSignedInt64 (final ByteOrder inOrder, final byte ... data)
        throws NumberFormatException
    {
        return readSignedInt64(inOrder, data, 0, (null == data) ? 0 : data.length);
    }

    public static final long readSignedInt64 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 8)
            throw new StreamCorruptedException("readSignedInt64(" + inOrder + ") insufficient data buffer size: " + len);

        FileIOUtils.readFully(inStream, workBuf, off, 8);
        return readSignedInt64(inOrder, workBuf, off, 8);
    }

    public static final long readSignedInt64 (
            final InputStream inStream, final ByteOrder inOrder, final byte[] workBuf)
        throws IOException
    {
        return readSignedInt64(inStream, inOrder, workBuf, 0, (null == workBuf) ? 0 : workBuf.length);
    }

    public static final long readSignedInt64 (final InputStream inStream, final ByteOrder inOrder) throws IOException
    {
        final byte[]    data={ 0, 0, 0, 0, 0, 0, 0, 0 };
        return readSignedInt64(inStream, inOrder, data);
    }
    // returns number of used bytes
    public static final int toInt16ByteArray (
            final int val, final ByteOrder outOrder, final byte[] buf, final int off)
        throws NumberFormatException
    {
        if (null == outOrder)
            throw new NumberFormatException("toInt16ByteArray(" + val + ") no order specified");

        final boolean    isBigEndian=ByteOrder.BIG_ENDIAN.equals(outOrder);
        buf[off] = (byte) ((isBigEndian ?  (val >> 8) : val) & 0x00FF);
        buf[off+1] = (byte) ((isBigEndian ?  val : (val >> 8)) & 0x00FF);
        return 2;
    }
    // returns number of used bytes
    public static final int toInt16ByteArray (
            final int val, final ByteOrder outOrder, final byte[] buf)
        throws NumberFormatException
    {
        return toInt16ByteArray(val, outOrder, buf, 0);
    }

    public static final byte[] toInt16ByteArray (
            final int val, final ByteOrder outOrder)
        throws NumberFormatException
    {
        final byte[]    data=new byte[2];
        final int        eLen=toInt16ByteArray(val, outOrder, data);
        if (eLen != data.length)
            throw new NumberFormatException("toInt16ByteArray(" + val + ")[" + outOrder + "] unexpected used length: expected=" + data.length + "/got=" + eLen);
        return data;
    }

    public static final byte[] toNativeInt16ByteArray (final int val)
    {
        return toInt16ByteArray(val, ByteOrder.nativeOrder());
    }
    // returns number of used/written bytes
    public static final int writeInt16 (
            final OutputStream outStream, final ByteOrder outOrder, final int val,
            final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 2)
            throw new StreamCorruptedException("writeInt16(" + val + ")[" + outOrder + "] insufficient work buffer length: " + len);

        toInt16ByteArray(val, outOrder, workBuf, off);
        outStream.write(workBuf, off, 2);
        return 2;
    }
    // returns number of used/written bytes
    public static final int writeInt16 (
            final OutputStream outStream, final ByteOrder outOrder, final int val, final byte[] workBuf)
        throws IOException
    {
        return writeInt16(outStream, outOrder, val, workBuf, 0, (null == workBuf) ? 0 : workBuf.length);
    }

    public static final byte[] writeInt16 (
            final OutputStream outStream, final ByteOrder outOrder, final int val) throws IOException
    {
        if ((null == outStream) || (null == outOrder))
            throw new IOException("writeInt16(" + outOrder + ")[" + val + "] incomplete arguments");

        final byte[]    data=toInt16ByteArray(val, outOrder);
        outStream.write(data);
        return data;
    }
    // returns number of used/written bytes
    public static final int writeUnsignedInt16 (
            final OutputStream outStream, final ByteOrder outOrder, final int val,
            final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if ((val > 0x00FFFF) || (val < 0))
            throw new StreamCorruptedException("writeUnsignedInt16(" + val + ")[" + outOrder + "] value exceeds max. unsigned int16 value");
        return writeInt16(outStream, outOrder, val, workBuf, off, len);
    }
    // returns number of used/written bytes
    public static final int writeUnsignedInt16 (
            final OutputStream outStream, final ByteOrder outOrder, final int val, final byte[] workBuf)
        throws IOException
    {
        return writeUnsignedInt16(outStream, outOrder, val, workBuf, 0, (null == workBuf) ? 0 : workBuf.length);
    }

    public static final byte[] writeUnsignedInt16 (
            final OutputStream outStream, final ByteOrder outOrder, final int val) throws IOException
    {
        if ((val > 0x00FFFF) || (val < 0))
            throw new StreamCorruptedException("writeUnsignedInt16(" + val + ")[" + outOrder + "] value exceeds max. unsigned int16 value");

        return writeInt16(outStream, outOrder, val & 0x00FFFF);
    }
    // returns number of used bytes
    public static final int toInt32ByteArray (
            final int val, final ByteOrder outOrder, final byte[] buf, final int off)
        throws NumberFormatException
    {
        if (null == outOrder)
            throw new NumberFormatException("toInt32ByteArray(" + val + ") no order specified");

        final boolean    isBigEndian=ByteOrder.BIG_ENDIAN.equals(outOrder);
        buf[off]      = (byte) ((isBigEndian ?  (val >> 24) : val) & 0x00FF);
        buf[off + 1] = (byte) ((isBigEndian ?  (val >> 16) : (val >> 8)) & 0x00FF);
        buf[off + 2] = (byte) ((isBigEndian ?  (val >> 8) : (val >> 16)) & 0x00FF);
        buf[off + 3] = (byte) ((isBigEndian ?  val : (val >> 24)) & 0x00FF);

        return 4;
    }

    public static final int toInt32ByteArray (
            final int val, final ByteOrder outOrder, final byte[] buf)
        throws NumberFormatException
    {
        return toInt32ByteArray(val, outOrder, buf, 0);
    }

    public static final byte[] toInt32ByteArray (
            final int val, final ByteOrder outOrder)
        throws NumberFormatException
    {
        final byte[]    data=new byte[4];
        final int        eLen=toInt32ByteArray(val, outOrder, data);
        if (eLen != data.length)
            throw new NumberFormatException("toInt32ByteArray(" + val + ")[" + outOrder + "] unexpected used length: expected=" + data.length + "/got=" + eLen);
        return data;
    }

    public static final byte[] toNativeInt32ByteArray (final int val)
    {
        return toInt32ByteArray(val, ByteOrder.nativeOrder());
    }
    // returns number of used/written bytes
    public static final int writeInt32 (
            final OutputStream outStream, final ByteOrder outOrder, final int val,
            final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 4)
            throw new StreamCorruptedException("writeInt32(" + val + ")[" + outOrder + "] insufficient work buffer length: " + len);

        toInt32ByteArray(val, outOrder, workBuf, off);
        outStream.write(workBuf, off, 4);
        return 4;
    }
    // returns number of used/written bytes
    public static final int writeInt32 (
            final OutputStream outStream, final ByteOrder outOrder, final int val, final byte[] workBuf)
        throws IOException
    {
        return writeInt32(outStream, outOrder, val, workBuf, 0, (null == workBuf) ? 0 : workBuf.length);
    }

    public static final byte[] writeInt32 (
            final OutputStream outStream, final ByteOrder outOrder, final int val) throws IOException
    {
        if ((null == outStream) || (null == outOrder))
            throw new IOException("writeInt32(" + outOrder + ")[" + val + "] incomplete arguments");

        final byte[]    data=toInt32ByteArray(val, outOrder);
        outStream.write(data);
        return data;
    }

    public static final byte[] writeUnsignedInt32 (final OutputStream out, final ByteOrder outOrder, final long val) throws IOException
    {
        if ((val > 0x00FFFFFFFFL) || (val < 0L))
            throw new StreamCorruptedException("writeUnsignedInt32(" + val + ")[" + outOrder + "] value exceeds max. unsigned int32 value");

        return writeInt32(out, outOrder, (int) (val & 0x00FFFFFFFFL));
    }
    // returns number of used/written bytes
    public static final int writeUnsignedInt32 (
            final OutputStream outStream, final ByteOrder outOrder, final long val,
            final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if ((val > 0x00FFFFFFFFL) || (val < 0L))
            throw new StreamCorruptedException("writeUnsignedInt32(" + val + ")[" + outOrder + "] value exceeds max. unsigned int32 value");
        return writeInt32(outStream, outOrder, (int) (val & 0x00FFFFFFFFL), workBuf, off, len);
    }
    // returns number of used/written bytes
    public static final int writeUnsignedInt32 (
            final OutputStream outStream, final ByteOrder outOrder, final long val, final byte[] workBuf)
        throws IOException
    {
        return writeUnsignedInt32(outStream, outOrder, val, workBuf, 0, (null == workBuf) ? 0 : workBuf.length);
    }

    // returns number of used bytes
    public static final int toInt64ByteArray (
            final long val, final ByteOrder outOrder, final byte[] buf, final int off)
        throws NumberFormatException
    {
        if (null == outOrder)
            throw new NumberFormatException("toInt64ByteArray(" + val + ") no order specified");

        if (ByteOrder.BIG_ENDIAN.equals(outOrder))
        {
            for (int    dIndex=0, bIndex=off, shiftSize=Long.SIZE - Byte.SIZE;
                 dIndex < 8;
                 dIndex++, shiftSize -= Byte.SIZE, bIndex++)
            {
                final long    dv=(shiftSize > 0) ? (val >> shiftSize) : val;
                buf[bIndex] = (byte) (dv & 0x00FFL);
            }
        }
        else
        {
            for (int    dIndex=off + 8, shiftSize=Long.SIZE - Byte.SIZE;
                 dIndex > off;
                 dIndex--, shiftSize -= Byte.SIZE)
            {
                final long    dv=(shiftSize > 0) ? (val >> shiftSize) : val;
                buf[dIndex - 1] = (byte) (dv & 0x00FFL);
            }
        }

        return 8;
    }

    public static final int toInt64ByteArray (
            final long val, final ByteOrder outOrder, final byte[] buf)
        throws NumberFormatException
    {
        return toInt64ByteArray(val, outOrder, buf, 0);
    }

    public static final byte[] toInt64ByteArray (
            final long val, final ByteOrder outOrder)
        throws NumberFormatException
    {
        final byte[]    data=new byte[Long.SIZE / Byte.SIZE];
        final int        eLen=toInt64ByteArray(val, outOrder, data);
        if (eLen != data.length)
            throw new NumberFormatException("toInt64ByteArray(" + val + ")[" + outOrder + "] unexpected used length: expected=" + data.length + "/got=" + eLen);
        return data;
    }

    public static final byte[] toNativeInt64ByteArray (final int val)
    {
        return toInt64ByteArray(val, ByteOrder.nativeOrder());
    }

    public static final byte[] writeInt64 (
            final OutputStream outStream, final ByteOrder outOrder, final long val) throws IOException
    {
        if ((null == outStream) || (null == outOrder))
            throw new IOException("writeInt64(" + outOrder + ")[" + val + "] incomplete arguments");

        final byte[]    data=toInt64ByteArray(val, outOrder);
        outStream.write(data);
        return data;
    }
    // returns number of used/written bytes
    public static final int writeInt64 (
            final OutputStream outStream, final ByteOrder outOrder, final long val,
            final byte[] workBuf, final int off, final int len)
        throws IOException
    {
        if (len < 8)
            throw new StreamCorruptedException("writeInt64(" + val + ")[" + outOrder + "] insufficient work buffer length: " + len);

        toInt64ByteArray(val, outOrder, workBuf, off);
        outStream.write(workBuf, off, 8);
        return 8;
    }
    // returns number of used/written bytes
    public static final int writeInt64 (
            final OutputStream outStream, final ByteOrder outOrder, final long val, final byte[] workBuf)
        throws IOException
    {
        return writeInt64(outStream, outOrder, val, workBuf, 0, (null == workBuf) ? 0 : workBuf.length);
    }
}
