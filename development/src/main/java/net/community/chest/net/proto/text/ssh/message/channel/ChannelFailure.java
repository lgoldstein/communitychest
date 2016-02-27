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
 * @since Jul 12, 2009 11:23:23 AM
 */
public class ChannelFailure extends AbstractSSHMsgEncoder<ChannelFailure> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6817470045829842600L;
	public ChannelFailure ()
	{
		super(SSHMsgCode.SSH_MSG_CHANNEL_FAILURE);
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
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public ChannelFailure read (final InputStream in) throws IOException
	{
		setRecipientChannel(SSHProtocol.readUint32(in));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public ChannelFailure decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setRecipientChannel(in.readInt());

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		SSHProtocol.writeUint32(out, getRecipientChannel());
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
	}
}
