/*
 * 
 */
package net.community.chest.net.proto.text.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.math.BigInteger;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.file.FileIOUtils;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;
import net.community.chest.util.compare.ByteArrayComparator;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 16, 2010 12:59:40 PM
 */
public class SSHMultiPrecisionInteger
		implements SSHDataObjectEncoder<SSHMultiPrecisionInteger>,
				   PubliclyCloneable<SSHMultiPrecisionInteger>{
	/**
	 * Number of valid bytes in the {@link #getData()} buffer
	 */
	private int	_length;
	public int getLength ()
	{
		return _length;
	}

	public void setLength (int length)
	{
		_length = length;
	}

	private byte[]	_data;
	public byte[] getData ()
	{
		return _data;
	}

	public void setData (byte ... data)
	{
		_data = data;
	}

	public void setData (int length, byte... data)
	{
		_length = length;
		_data = data;
	}

	public BigInteger toBigInteger () throws NumberFormatException
	{
		final int	length=getLength();
		if (length <= 0)
			throw new NumberFormatException("toBigInteger() bad length: " + length);

		final byte[]	data=getData();
		if ((null == data) || (data.length < length))
			throw new NumberFormatException("toBigInteger() bad data bytes");

		if (data.length != length)
		{
			final byte[]	dataCopy=new byte[length];
			System.arraycopy(data, 0, dataCopy, 0, length);
			return new BigInteger(dataCopy);
		}

		return new BigInteger(data);
	}

	public SSHMultiPrecisionInteger (int length, byte...data)
	{
		_length = length;
		_data = data;
	}
	
	public SSHMultiPrecisionInteger ()
	{
		this(0, null);
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public SSHMultiPrecisionInteger decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getClass().getSimpleName() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		final byte[]	data=in.readBlob();
		final int		length=(null == data) ? 0 : data.length;
		setData(length, data);
		return this;
	}

	public SSHMultiPrecisionInteger (SSHInputDataDecoder in) throws IOException
	{
		decode(in);
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getClass().getSimpleName() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		out.writeBlob(getData(), 0, getLength());
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public SSHMultiPrecisionInteger read (InputStream in) throws IOException
	{
		final int	length=SSHProtocol.readUint32(in);
		if (length < 0)
			throw new StreamCorruptedException("read(" + getClass().getSimpleName() + ") bad data length: " + length);

		final byte[]	data=new byte[length];
		if (length > 0)
			FileIOUtils.readFully(in,data, 0, length);

		return this;
	}

	public SSHMultiPrecisionInteger (InputStream in) throws IOException
	{
		read(in);
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		final int	length=getLength();
		if (length < 0)
			throw new StreamCorruptedException("write(" + getClass().getSimpleName() + ") bad data length: " + length);
		SSHProtocol.writeUint32(out, length);

		if (length > 0)
		{
			final byte[]	data=getData();
			final int		dLen=(null == data) ? 0 : data.length;
			if (dLen < length)
				throw new StreamCorruptedException("write(" + getClass().getSimpleName() + ") insufficient data: got=" + dLen + "/expected=" + length);
			
			out.write(data, 0, length);
		}
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public SSHMultiPrecisionInteger clone () throws CloneNotSupportedException
	{
		final SSHMultiPrecisionInteger	ret=getClass().cast(super.clone());
		final byte[]					data=ret.getData();
		final int						length=ret.getLength();
		if (length > 0)
			ret.setData(data.clone());
		else
			ret.setData(null);

		return ret;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof SSHMultiPrecisionInteger))
			return false;

		final SSHMultiPrecisionInteger	other=(SSHMultiPrecisionInteger) obj;
		if ((getLength() != other.getLength())
		 || (ByteArrayComparator.ASCENDING.compare(getData(), other.getData()) != 0))
		 	return false;

		return true;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return getLength() + ByteArrayComparator.getHashCode(getData());
	}
}
