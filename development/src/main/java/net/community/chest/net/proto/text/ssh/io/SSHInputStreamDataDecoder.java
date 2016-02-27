/*
 * 
 */
package net.community.chest.net.proto.text.ssh.io;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;

import net.community.chest.io.encode.endian.AbstractEndianInputDecoder;
import net.community.chest.io.file.FileIOUtils;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 13, 2009 9:03:59 AM
 */
public class SSHInputStreamDataDecoder extends AbstractEndianInputDecoder
					implements SSHInputDataDecoder {
	/*
	 * @see net.community.chest.io.OptionallyCloseable#isMutableRealClosure()
	 */
	@Override
	public boolean isMutableRealClosure ()
	{
		return true;
	}

	private boolean	_realClosure;
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

	private InputStream	_in;
	/*
	 * @see net.community.chest.io.IOAccessEmbedder#getEmbeddedAccess()
	 */
	@Override
	public InputStream getEmbeddedAccess ()
	{
		return _in;
	}
	/*
	 * @see net.community.chest.io.IOAccessEmbedder#setEmbeddedAccess(java.lang.Object)
	 */
	@Override
	public void setEmbeddedAccess (InputStream in) throws IOException
	{
		_in = in;
	}
	/*
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen ()
	{
		return getEmbeddedAccess() != null;
	}

	public SSHInputStreamDataDecoder (final InputStream in, final boolean realClose)
	{
		super(ByteOrder.BIG_ENDIAN);

		_in = in;
		_realClosure = realClose;
	}

	public SSHInputStreamDataDecoder (final InputStream in)
	{
		this(in, true);
	}

	public SSHInputStreamDataDecoder ()
	{
		this(null);
	}
	/*
	 * @see net.community.chest.io.encode.endian.AbstractEndianInputDecoder#isMutableByteOrder()
	 */
	@Override
	public boolean isMutableByteOrder ()
	{
		return false;
	}
	/*
	 * @see net.community.chest.io.encode.endian.AbstractEndianInputDecoder#setByteOrder(java.nio.ByteOrder)
	 */
	@Override
	public void setByteOrder (ByteOrder o)
	{
		if (!ByteOrder.BIG_ENDIAN.equals(o))
			throw new UnsupportedOperationException("setByteOrder(" + o + ") N/A");
	}
	/*
	 * @see java.io.DataInput#readByte()
	 */
	@Override
	public byte readByte () throws IOException
	{
		if (!isOpen())
			throw new IOException("readByte() not open");

		final InputStream	in=getEmbeddedAccess();
		if (null == in)
			throw new StreamCorruptedException("readByte() no " + InputStream.class.getSimpleName() + " instance");

		final int	val=in.read();
		if (val == (-1))
			throw new EOFException("readByte() EOF");
		return (byte) (val & 0x00FF);
	}
	/*
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 */
	@Override
	public void readFully (byte[] buf, int off, int len) throws IOException
	{
		if (!isOpen())
			throw new IOException("readFully(" + len + ") not open");

		FileIOUtils.readFully(getEmbeddedAccess(), buf, off, len);
	}
	/*
	 * @see net.community.chest.io.encode.endian.AbstractEndianInputDecoder#readBoolean()
	 */
	/*
	 * @see net.community.chest.io.encode.endian.AbstractEndianInputDecoder#readBoolean()
	 */
	@Override
	public boolean readBoolean () throws IOException
	{
		return (readByte() != 0);
	}
	/*
	 * @see net.community.chest.io.encode.endian.AbstractEndianInputDecoder#readString(java.lang.String)
	 */
	@Override
	public String readString (final String charsetName) throws IOException
	{
		final int	sLen=readInt();
		if (sLen < 0)	// we do not expect strings >2GB
			throw new StreamCorruptedException("readString(" + charsetName + ") bad string length: " + sLen);

		final byte[]	workBuf=getWorkBuf(sLen, true);
		readFully(workBuf, 0, sLen);
		return new String(workBuf, 0, sLen, charsetName);
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder#readBlob(byte[], int, int)
	 */
	@Override
	public int readBlob (byte[] buf, int off, int len) throws IOException
	{
		final int	sLen=readInt();
		if (sLen < 0)	// we do not expect strings >2GB
			throw new StreamCorruptedException("readBlob(" + off + "/" + len + ") bad data length: " + sLen);

		if (sLen > len)
			throw new StreamCorruptedException("readBlob(" + off + "/" + len + ") bad buffer length: required=" + sLen + "/available=" + len);

		if (sLen > 0)
			readFully(buf, off, sLen);
		return sLen;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder#readBlob()
	 */
	@Override
	public byte[] readBlob () throws IOException
	{
		final int	sLen=readInt();
		if (sLen < 0)	// we do not expect data >2GB
			throw new StreamCorruptedException("readBlob() bad data length: " + sLen);

		final byte[]	data=new byte[sLen];
		if (sLen > 0)
			readFully(data, 0, sLen);
		return data;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder#readBlob(byte[])
	 */
	@Override
	public int readBlob (byte[] buf) throws IOException
	{
		return readBlob(buf, 0, (null == buf) ? 0 : buf.length);
	}
	/*
	 * @see net.community.chest.io.encode.InputDataDecoder#readString(java.nio.charset.Charset)
	 */
	@Override
	public String readString (Charset charset) throws IOException
	{
		return readString(charset.name());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder#readASCII()
	 */
	@Override
	public String readASCII () throws IOException
	{
		return readString("US-ASCII");
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder#readNamesList()
	 */
	@Override
	public List<String> readNamesList () throws IOException
	{
		final String	nl=readASCII();
		return StringUtil.splitString(nl, ',');
	}
	/*
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close () throws IOException
	{
		final Closeable	s=getEmbeddedAccess();
		if (s != null)
		{
			try
			{
				if (isRealClosure())
					s.close();
			}
			finally
			{
				setEmbeddedAccess(null);
			}
		}
	}
}
