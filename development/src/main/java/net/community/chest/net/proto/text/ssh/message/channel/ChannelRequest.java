/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

import net.community.chest.net.proto.text.ssh.SSHMsgCode;
import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;
import net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 12, 2009 11:28:42 AM
 */
public class ChannelRequest extends AbstractSSHMsgEncoder<ChannelRequest> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7765797702127029837L;
	public ChannelRequest ()
	{
		super(SSHMsgCode.SSH_MSG_CHANNEL_REQUEST);
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

	private String	_requestType;
	public String getRequestType ()
	{
		return _requestType;
	}

	public void setRequestType (String t)
	{
		_requestType = t;
	}
	// null == not-initialized
	private Boolean	_replyRequested;
	public Boolean getReplyRequested ()
	{
		return _replyRequested;
	}

	public void setReplyRequested (Boolean replyRequested)
	{
		_replyRequested = replyRequested;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public ChannelRequest read (InputStream in) throws IOException
	{
		setRecipientChannel(SSHProtocol.readUint32(in));
		setRequestType(SSHProtocol.readASCIIString(in));
		setReplyRequested(Boolean.valueOf(SSHProtocol.readBoolean(in)));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public ChannelRequest decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setRecipientChannel(in.readInt());
		setRequestType(in.readASCII());
		setReplyRequested(Boolean.valueOf(in.readBoolean()));

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		final Boolean	rr=getReplyRequested();
		if (null == rr)
			throw new StreamCorruptedException("write(" + getMsgCode() + ") - reply-requested value not set");

		SSHProtocol.writeUint32(out, getRecipientChannel());
		SSHProtocol.writeStringBytes(out, true, getRequestType());
		SSHProtocol.writeBoolean(out, rr.booleanValue());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getMsgCode() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		final Boolean	rr=getReplyRequested();
		if (null == rr)
			throw new StreamCorruptedException("encode(" + getMsgCode() + ") - reply-requested value not set");

		out.writeInt(getRecipientChannel());
		out.writeASCII(getRequestType());
		out.writeBoolean(rr.booleanValue());
	}
}
