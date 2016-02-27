/*
 * 
 */
package net.community.chest.net.proto.text.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.lang.PubliclyCloneable;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 2, 2009 2:36:17 PM
 */
public class SSHPacketHeader implements ElementEncoder<SSHPacketHeader>,
										PubliclyCloneable<SSHPacketHeader>,
										Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8162865449387179771L;
	public SSHPacketHeader ()
	{
		super();
	}

	private int	_length;
	public int getLength ()
	{
		return _length;
	}

	public void setLength (int length)
	{
		_length = length;
	}

	private short	_padLength;
	public short getPadLength ()
	{
		return _padLength;
	}

	public void setPadLength (short padLength)
	{
		_padLength = padLength;
	}

	public int getPayloadLength ()
	{
		return getLength() - getPadLength() - 1;
	}
	// NOTE !!! not read/written - also, array length not necessarily exactly of payload length
	private byte[]	_payloadData;
	public byte[] getPayloadData ()
	{
		return _payloadData;
	}

	public void setPayloadData (byte[] payloadData)
	{
		_payloadData = payloadData;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public SSHPacketHeader read (InputStream in) throws IOException
	{
		setLength(SSHProtocol.readUint32(in));
		setPadLength(SSHProtocol.readByte(in));
		return this;
	}

	public SSHPacketHeader (InputStream in) throws IOException
	{
		read(in);
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		SSHProtocol.writeUint32(out, getLength());
		out.write(getPadLength() & 0x00FF);
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public SSHPacketHeader clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof SSHPacketHeader))
			return false;
		if (this == obj)
			return true;
		
		final SSHPacketHeader	h=(SSHPacketHeader) obj;
		return (h.getLength() == getLength())
			&& (h.getPadLength() == getPadLength())
			;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return getLength() + getPadLength();
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return "Length=" + getLength() + ";Pad=" + getPadLength();
	}
}
