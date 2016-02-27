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
 * See RFC 4254
 * @author Lyor G.
 * @since Jul 12, 2009 10:54:09 AM
 */
public class ChannelOpen extends AbstractSSHMsgEncoder<ChannelOpen> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6810908700442358957L;
	public ChannelOpen ()
	{
		super(SSHMsgCode.SSH_MSG_CHANNEL_OPEN);
	}

	private String	_channelType;
	public String getChannelType ()
	{
		return _channelType;
	}

	public void setChannelType (String channelType)
	{
		_channelType = channelType;
	}

	private int	_senderChannel;
	public int getSenderChannel ()
	{
		return _senderChannel;
	}

	public void setSenderChannel (int senderChannel)
	{
		_senderChannel = senderChannel;
	}

	private int	_initialWindowSize;
	public int getInitialWindowSize ()
	{
		return _initialWindowSize;
	}

	public void setInitialWindowSize (int initialWindowSize)
	{
		_initialWindowSize = initialWindowSize;
	}

	private int	_maxPacketSize;
	public int getMaxPacketSize ()
	{
		return _maxPacketSize;
	}

	public void setMaxPacketSize (int maxPacketSize)
	{
		_maxPacketSize = maxPacketSize;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public ChannelOpen read (InputStream in) throws IOException
	{
		setChannelType(SSHProtocol.readASCIIString(in));
		setSenderChannel(SSHProtocol.readUint32(in));
		setInitialWindowSize(SSHProtocol.readUint32(in));
		setMaxPacketSize(SSHProtocol.readUint32(in));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public ChannelOpen decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setChannelType(in.readASCII());
		setSenderChannel(in.readInt());
		setInitialWindowSize(in.readInt());
		setMaxPacketSize(in.readInt());

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		SSHProtocol.writeStringBytes(out, true, getChannelType());
		SSHProtocol.writeUint32(out, getSenderChannel());
		SSHProtocol.writeUint32(out, getInitialWindowSize());
		SSHProtocol.writeUint32(out, getMaxPacketSize());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getMsgCode() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		out.writeASCII(getChannelType());
		out.writeInt(getSenderChannel());
		out.writeInt(getInitialWindowSize());
		out.writeInt(getMaxPacketSize());
	}
}
