/*
 * 
 */
package net.community.chest.net.proto.text.ssh.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.Collection;

import net.community.chest.io.encode.endian.AbstractEndianOutputEncoder;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 13, 2009 12:52:41 PM
 */
public class SSHOutputStreamDataEncoder extends AbstractEndianOutputEncoder
		implements SSHOutputDataEncoder {
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

	private OutputStream	_out;
	/*
	 * @see net.community.chest.io.IOAccessEmbedder#getEmbeddedAccess()
	 */
	@Override
	public OutputStream getEmbeddedAccess ()
	{
		return _out;
	}
	/*
	 * @see net.community.chest.io.IOAccessEmbedder#setEmbeddedAccess(java.lang.Object)
	 */
	@Override
	public void setEmbeddedAccess (OutputStream out) throws IOException
	{
		_out= out;
	}
	/*
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen ()
	{
		return getEmbeddedAccess() != null;
	}

	public SSHOutputStreamDataEncoder (final OutputStream out, final boolean realClose)
	{
		super(ByteOrder.BIG_ENDIAN);

		_out = out;
		_realClosure = realClose;
	}

	public SSHOutputStreamDataEncoder (final OutputStream out)
	{
		this(out, true);
	}

	public SSHOutputStreamDataEncoder ()
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
	 * @see net.community.chest.io.encode.endian.AbstractEndianOutputEncoder#writeStringBytes(java.lang.String, java.nio.charset.CharsetEncoder, byte[], int, int)
	 */
	@Override
	protected void writeStringBytes (String s, CharsetEncoder charsetEnc, byte[] data, int off, int len) throws IOException
	{
		writeInt(len);
		write(data, off, len);
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder#writeASCII(java.lang.String)
	 */
	@Override
	public void writeASCII (String s) throws IOException
	{
		writeString(s, "US-ASCII");
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder#writeBlob(byte[], int, int)
	 */
	@Override
	public void writeBlob (byte[] buf, int off, int len) throws IOException
	{
		writeInt(len);
		write(buf, off, len);
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder#writeBlob(byte[])
	 */
	@Override
	public void writeBlob (byte[] buf) throws IOException
	{
		write(buf, 0, (null == buf) ? 0 : buf.length);
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder#writeNamesList(java.util.Collection)
	 */
	@Override
	public void writeNamesList (final Collection<?> nl) throws IOException
	{
		final String	sl=StringUtil.asStringList(nl, ',');
		writeASCII(sl);
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder#writeNamesList(java.lang.Object[])
	 */
	@Override
	public void writeNamesList (Object... nl) throws IOException
	{
		writeNamesList(((null == nl) || (nl.length <= 0)) ? null : Arrays.asList(nl));
	}
	/*
	 * @see java.io.Flushable#flush()
	 */
	@Override
	public void flush () throws IOException
	{
		if (!isOpen())
			throw new IOException("flush() not open");

		final OutputStream	out=getEmbeddedAccess();
		if (null == out)
			throw new IOException("flush() no " + OutputStream.class.getSimpleName() + " instance");

		out.flush();
	}
	/*
	 * @see java.io.DataOutput#write(byte[], int, int)
	 */
	@Override
	public void write (byte[] b, int off, int len) throws IOException
	{
		if (!isOpen())
			throw new IOException("write(" + off + "/" + len + ") not open");

		final OutputStream	out=getEmbeddedAccess();
		if (null == out)
			throw new IOException("write(" + off + "/" + len + ") no " + OutputStream.class.getSimpleName() + " instance");

		out.write(b, off, len);
	}
	/*
	 * @see java.io.DataOutput#writeByte(int)
	 */
	@Override
	public void writeByte (int v) throws IOException
	{
		if (!isOpen())
			throw new IOException("writeByte(" + v + ") not open");

		final OutputStream	out=getEmbeddedAccess();
		if (null == out)
			throw new IOException("writeByte(" + v + ") no " + OutputStream.class.getSimpleName() + " instance");

		out.write(v);
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
