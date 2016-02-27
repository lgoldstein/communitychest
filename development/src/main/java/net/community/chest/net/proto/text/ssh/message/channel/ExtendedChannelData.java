/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.community.chest.net.proto.text.ssh.SSHMsgCode;
import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;
import net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 12, 2009 11:13:02 AM
 */
public class ExtendedChannelData extends AbstractSSHMsgEncoder<ExtendedChannelData> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4946862017471635499L;
	public ExtendedChannelData ()
	{
		super(SSHMsgCode.SSH_MSG_CHANNEL_EXTENDED_DATA);
	}

	private int	_recipientChannel;
	public int getRecipientChannel ()
	{
		return _recipientChannel;
	}

	public void setRecipientChannel (int recipientChannel)
	{
		_recipientChannel = recipientChannel;
	}

	public static final int	SSH_EXTENDED_DATA_STDERR=1;	/* as per RFC 4254 */

	private int	_dataTypeCode;
	public int getDataTypeCode ()
	{
		return _dataTypeCode;
	}

	public void setDataTypeCode (int dataTypeCode)
	{
		_dataTypeCode = dataTypeCode;
	}
	// NOTE !!! this is actually part of the "string" data
	private int	_dataLen;
	public int getDataLen ()
	{
		return _dataLen;
	}

	public void setDataLen (int dataLen)
	{
		_dataLen = dataLen;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public ExtendedChannelData read (final InputStream in) throws IOException
	{
		setRecipientChannel(SSHProtocol.readUint32(in));
		setDataTypeCode(SSHProtocol.readUint32(in));
		setDataLen(SSHProtocol.readStringLength(in));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public ExtendedChannelData decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setRecipientChannel(in.readInt());
		setDataTypeCode(in.readInt());
		setDataLen(in.readInt());

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		SSHProtocol.writeUint32(out, getRecipientChannel());
		SSHProtocol.writeUint32(out, getDataTypeCode());
		SSHProtocol.writeStringLength(out, getDataLen());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getMsgCode() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		out.writeInt(getRecipientChannel());
		out.writeInt(getDataTypeCode());
		out.writeInt(getDataLen());
	}
}
