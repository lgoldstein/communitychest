/*
 * 
 */
package net.community.chest.io.encode.endian;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import net.community.chest.io.encode.InputDataDecoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 13, 2009 8:40:17 AM
 */
public abstract class AbstractEndianInputDecoder
		implements InputDataDecoder, ByteOrderControlled {
	private ByteOrder	_inOrder;
	/*
	 * @see net.community.chest.io.encode.endian.ByteOrderControlled#getByteOrder()
	 */
	@Override
	public ByteOrder getByteOrder ()
	{
		return _inOrder;
	}
	/*
	 * @see net.community.chest.io.encode.endian.ByteOrderControlled#setByteOrder(java.nio.ByteOrder)
	 */
	@Override
	public void setByteOrder (ByteOrder o)
	{
		_inOrder = o;
	}
	/*
	 * @see net.community.chest.io.encode.endian.ByteOrderControlled#isMutableByteOrder()
	 */
	@Override
	public boolean isMutableByteOrder ()
	{
		return true;
	}

	protected AbstractEndianInputDecoder (final ByteOrder inOrder)
	{
		_inOrder = inOrder;
	}
	
	protected AbstractEndianInputDecoder ()
	{
		this(null);
	}

	private byte[]	_workBuf;
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
		final byte[]	prev=_workBuf;
		_workBuf = buf;
		return prev;
	}
	/*
	 * @see java.io.DataInput#readFully(byte[])
	 */
	@Override
	public void readFully (byte[] b) throws IOException
	{
		readFully(b, 0, b.length);
	}
	/*
	 * @see java.io.DataInput#readInt()
	 */
	@Override
	public int readInt () throws IOException
	{
		final byte[]	workBuf=getWorkBuf(4, true);
		readFully(workBuf, 0, 4);
		try
		{
			return EndianEncoder.readSignedInt32(getByteOrder(), workBuf, 0, 4);
		}
		catch(NumberFormatException e)
		{
			throw new IOException("readInt() " + e.getMessage());
		}
	}
	/*
	 * @see java.io.DataInput#readLong()
	 */
	@Override
	public long readLong () throws IOException
	{
		final byte[]	workBuf=getWorkBuf(8, true);
		readFully(workBuf, 0, 8);
		try
		{
			return EndianEncoder.readSignedInt64(getByteOrder(), workBuf, 0, 8);
		}
		catch(NumberFormatException e)
		{
			throw new IOException("readLong() " + e.getMessage());
		}
	}
	/*
	 * @see java.io.DataInput#readShort()
	 */
	@Override
	public short readShort () throws IOException
	{
		final byte[]	workBuf=getWorkBuf(2, true);
		readFully(workBuf, 0, 2);
		try
		{
			return EndianEncoder.readSignedInt16(getByteOrder(), workBuf, 0, 2);
		}
		catch(NumberFormatException e)
		{
			throw new IOException("readShort() " + e.getMessage());
		}
	}
	/*
	 * @see java.io.DataInput#readUnsignedByte()
	 */
	@Override
	public int readUnsignedByte () throws IOException
	{
		return (readByte() & 0x00FF);
	}
	/*
	 * @see java.io.DataInput#readUnsignedShort()
	 */
	@Override
	public int readUnsignedShort () throws IOException
	{
		final byte[]	workBuf=getWorkBuf(2, true);
		readFully(workBuf, 0, 2);
		try
		{
			return EndianEncoder.readUnsignedInt16(getByteOrder(), workBuf, 0, 2);
		}
		catch(NumberFormatException e)
		{
			throw new IOException("readUnsignedShort() " + e.getMessage());
		}
	}
	/*
	 * @see net.community.chest.io.encode.InputDataDecoder#readString(java.nio.charset.CharsetDecoder)
	 */
	@Override
	public String readString (CharsetDecoder charsetDec) throws IOException
	{
		return readString(charsetDec.charset());
	}
	/*
	 * @see net.community.chest.io.encode.InputDataDecoder#readString(java.lang.String)
	 */
	@Override
	public String readString (String charsetName) throws IOException
	{
		return readString(Charset.forName(charsetName));
	}
	/*
	 * @see java.io.DataInput#readUTF()
	 */
	@Override
	public String readUTF () throws IOException
	{
		return readString("UTF-8");
	}
	/*
	 * @see java.io.DataInput#readBoolean()
	 */
	@Override
	public boolean readBoolean () throws IOException
	{
		throw new StreamCorruptedException("readBoolean() N/A");
	}
	/*
	 * @see java.io.DataInput#readChar()
	 */
	@Override
	public char readChar () throws IOException
	{
		throw new StreamCorruptedException("readChar() N/A");
	}
	/*
	 * @see java.io.DataInput#readDouble()
	 */
	@Override
	public double readDouble () throws IOException
	{
		throw new StreamCorruptedException("readDouble() N/A");
	}
	/*
	 * @see java.io.DataInput#readFloat()
	 */
	@Override
	public float readFloat () throws IOException
	{
		throw new StreamCorruptedException("readFloat() N/A");
	}
	/*
	 * @see java.io.DataInput#readLine()
	 */
	@Override
	public String readLine () throws IOException
	{
		throw new StreamCorruptedException("readLine() N/A");
	}
	/*
	 * @see java.io.DataInput#skipBytes(int)
	 */
	@Override
	public int skipBytes (int n) throws IOException
	{
		throw new StreamCorruptedException("skipBytes(" + n + ") N/A");
	}
}
