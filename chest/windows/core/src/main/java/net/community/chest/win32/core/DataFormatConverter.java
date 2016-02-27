/*
 * 
 */
package net.community.chest.win32.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.community.chest.io.encode.endian.EndianEncoder;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.datetime.DateUtil;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 15, 2009 2:05:54 PM
 */
public final class DataFormatConverter {
	private DataFormatConverter ()
	{
		// no instance
	}

	public static final byte readByte (final InputStream in) throws IOException
	{
		if (null == in)
			throw new IOException("readByte() no " + InputStream.class.getSimpleName() + " provided");

		final int	val=in.read();
		if (val == (-1))
			throw new EOFException("readByte() no more data");

		return (byte) (val & 0x00FF);
	}

	public static final short readUnsignedByte (final InputStream in) throws IOException
	{
		return (short) (readByte(in) & 0x00FF);
	}

	public static final short readSignedInt16 (final InputStream in) throws IOException
	{
		return EndianEncoder.readSignedInt16(in, ByteOrder.LITTLE_ENDIAN);
	}
	
	public static final int readUnsignedInt16 (final InputStream in) throws IOException
	{
		return EndianEncoder.readUnsignedInt16(in, ByteOrder.LITTLE_ENDIAN);
	}
	
	public static final List<Integer> readSignedInt32List(final int numValues, final InputStream in)
			throws IOException
	{
		if (numValues <= 0)
			return Collections.emptyList();
		
		List<Integer>	values=new ArrayList<Integer>(numValues);
		readSignedInt32List(numValues, in, values);
		return values;
	}

	public static final void readSignedInt32List(final int numValues, final InputStream in, final List<Integer> values)
			throws IOException
	{
		for (int index=0; index < numValues; index++)
		{
			int	v=readSignedInt32(in);
			values.add(Integer.valueOf(v));
		}
	}

	public static final int readSignedInt32 (final InputStream in) throws IOException
	{
		return EndianEncoder.readSignedInt32(in, ByteOrder.LITTLE_ENDIAN);
	}

	public static final long readUnsignedInt32 (final InputStream in) throws IOException
	{
		return EndianEncoder.readUnsignedInt32(in, ByteOrder.LITTLE_ENDIAN);
	}

	public static final double readDouble (final InputStream in) throws IOException
	{
		long	bits=readSignedInt64(in);
		return Double.longBitsToDouble(bits);
	}

	public static final long readSignedInt64 (final InputStream in) throws IOException
	{
		return EndianEncoder.readSignedInt64(in, ByteOrder.LITTLE_ENDIAN);
	}

	public static final byte[] writeSignedInt16 (final OutputStream out, final short val) throws IOException
	{
		return EndianEncoder.writeInt16(out, ByteOrder.LITTLE_ENDIAN, val & 0x00FFFF);
	}

	public static final byte[] writeUnsignedInt16 (final OutputStream out, final int val) throws IOException
	{
		return EndianEncoder.writeUnsignedInt16(out, ByteOrder.LITTLE_ENDIAN, val);
	}

	public static final void writeSignedInt32List(final OutputStream out, final Collection<? extends Number> values) throws IOException
	{
		if (CollectionsUtils.size(values) <= 0)
			return;

		final byte[]	data=new byte[4];
		for (final Number n : values)
		{
			final int	eLen=EndianEncoder.toInt32ByteArray(n.intValue(), ByteOrder.LITTLE_ENDIAN, data);
			if (eLen != data.length)
				throw new StreamCorruptedException("writeSignedInt32List(" + n + ") unexpected used length: expected=" + data.length + "/got=" + eLen);
			out.write(data);
		}
	}

	public static final byte[] writeSignedInt32 (final OutputStream out, final int val) throws IOException
	{
		return EndianEncoder.writeInt32(out, ByteOrder.LITTLE_ENDIAN, val);
	}

	public static final byte[] writeUnsignedInt32 (final OutputStream out, final long val) throws IOException
	{
		return EndianEncoder.writeUnsignedInt32(out, ByteOrder.LITTLE_ENDIAN, val);
	}

	public static final byte[] writeDouble (final OutputStream out, final double val) throws IOException
	{
		long	bits=Double.doubleToLongBits(val);
		return writeInt64(out, bits);
	}

	public static final byte[] writeInt64 (final OutputStream out, final long val) throws IOException
	{
		return EndianEncoder.writeInt64(out, ByteOrder.LITTLE_ENDIAN, val);
	}

	public static final void writeByte (final OutputStream out, final byte val)throws IOException
	{
		if (null == out)
			throw new IOException("writeByte() no " + OutputStream.class.getSimpleName() + " provided");
		out.write(val & 0x00FF);
	}

	public static final void writeUnsignedByte (final OutputStream out, final short val)throws IOException
	{
		if (val > 0x00FF)
			throw new StreamCorruptedException("writeUnsignedByte(" + val + ") value exceeds max. unsigned byte value");

		writeByte(out, (byte) (val & 0x00FF));
	}

	public static final long toJVMTimestamp (final long tsVal)
	{
		return tsVal * DateUtil.MSEC_PER_SECOND;
	}

	public static final long toPETimestamp (final long tsVal)
	{
		return tsVal / DateUtil.MSEC_PER_SECOND;
	}

	public static final String toString (final char ... chars)
	{
		final int	numChars=(null == chars) ? 0 : chars.length;
		for (int	nullIndex=0; nullIndex < numChars; nullIndex++)
		{
			if ('\0' == chars[nullIndex])
			{
				if (nullIndex > 0)
					return new String(chars, 0, nullIndex);
				else
					return "";
			}
		}

		return (numChars <= 0) ? "" : new String(chars);
	}

	public static final String toAsciiString (final byte[] bytes, final int off, final int numBytes)
	{
		final int	maxPos=Math.max(off,0) + Math.max(numBytes,0);
		int			nullIndex=off;
		for ( ; nullIndex < maxPos; nullIndex++)
		{
			if (0 == bytes[nullIndex])
				break;
		}

		if (nullIndex <= off)
			return "";

		final char[]	chars=new char[nullIndex - off];
		for (int	cIndex=0, bIndex=off; bIndex < nullIndex; cIndex++, bIndex++)
			chars[cIndex] = (char) (bytes[bIndex] & 0x00FF);
		return new String(chars);
	}

	public static final String toAsciiString (final byte ... bytes)
	{
		return toAsciiString(bytes, 0, (null == bytes) ? 0 : bytes.length);
	}

	public static final char[] toAsciiCharArray (final String s)
	{
		final int		sLen=(null == s) ? 0 : s.length();
		final char[]	chars=new char[Math.max(sLen, 0) + 1 /* for the '\0' */];
		if (sLen > 0)
			s.getChars(0, sLen, chars, 0);
		chars[chars.length - 1] = '\0';
		return chars;
	}

	public static final byte[] toAsciiByteArray (final String s) throws UnsupportedEncodingException
	{
		final byte[]	sb=((null == s) || (s.length() <= 0)) ? null : s.getBytes("UTF-8");
		final int		sbLen=(null == sb) ? 0 : sb.length;
		final byte[]	bytes=new byte[sbLen + 1];
		if (sbLen > 0)
			System.arraycopy(sb, 0, bytes, 0, sbLen);
		bytes[sbLen] = 0;
		return bytes;
	}

	public static final String toUnicodeString (final byte[] bytes, final int off, final int numBytes)
	{
		if (numBytes <= 0)
			return "";

		if ((numBytes & 1) != 0)
			throw new IllegalArgumentException("toUnicodeString(len=" + numBytes + ") incomplete data");

		final char[]	chars=new char[numBytes /  2];
		for (int	dIndex=off, cIndex=0; dIndex < numBytes; dIndex += 2, cIndex++)
		{
			final char	c=(char) (((bytes[dIndex + 1] << 8) & 0x00FF00) | (bytes[dIndex] & 0x00FF));
			chars[cIndex] = c;
		}

		return new String(chars);
	}

	public static final String toUnicodeString (final byte... bytes)
	{
		return toUnicodeString(bytes, 0, (null == bytes) ? 0 : bytes.length);
	}
}
