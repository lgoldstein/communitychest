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
 * @since Jul 12, 2009 10:44:48 AM
 */
public class ChannelData extends AbstractSSHMsgEncoder<ChannelData> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8054733052695283741L;
	public ChannelData ()
	{
		super(SSHMsgCode.SSH_MSG_CHANNEL_DATA);
	}

	private int	_channelNumber;
	public int getChannelNumber ()
	{
		return _channelNumber;
	}

	public void setChannelNumber (int channelNumber)
	{
		_channelNumber = channelNumber;
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
	public ChannelData read (InputStream in) throws IOException
	{
		setChannelNumber(SSHProtocol.readUint32(in));
		setDataLen(SSHProtocol.readStringLength(in));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public ChannelData decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setChannelNumber(in.readInt());
		setDataLen(in.readInt());

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		SSHProtocol.writeUint32(out, getChannelNumber());
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

		out.writeInt(getChannelNumber());
		out.writeInt(getDataLen());
	}
}
