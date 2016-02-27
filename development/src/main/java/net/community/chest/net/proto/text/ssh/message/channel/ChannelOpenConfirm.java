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
 * @since Jul 12, 2009 10:59:25 AM
 */
public class ChannelOpenConfirm extends AbstractSSHMsgEncoder<ChannelOpenConfirm> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7409760962508610317L;
	public ChannelOpenConfirm ()
	{
		super(SSHMsgCode.SSH_MSG_CHANNEL_OPEN_CONFIRMATION);
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
	public ChannelOpenConfirm read (final InputStream in) throws IOException
	{
		setRecipientChannel(SSHProtocol.readUint32(in));
		setSenderChannel(SSHProtocol.readUint32(in));
		setInitialWindowSize(SSHProtocol.readUint32(in));
		setMaxPacketSize(SSHProtocol.readUint32(in));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public ChannelOpenConfirm decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setRecipientChannel(in.readInt());
		setSenderChannel(in.readInt());
		setInitialWindowSize(in.readInt());
		setMaxPacketSize(in.readInt());

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		SSHProtocol.writeUint32(out, getRecipientChannel());
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

		out.writeInt(getRecipientChannel());
		out.writeInt(getSenderChannel());
		out.writeInt(getInitialWindowSize());
		out.writeInt(getMaxPacketSize());
	}
}
