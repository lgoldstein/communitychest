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
 * @since Jul 12, 2009 11:04:07 AM
 */
public class ChannelOpenFailure extends AbstractSSHMsgEncoder<ChannelOpenFailure> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2561156519465698815L;
	public ChannelOpenFailure ()
	{
		super(SSHMsgCode.SSH_MSG_CHANNEL_OPEN_FAILURE);
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

	private int	_reasonCode;
	public int getReasonCode ()
	{
		return _reasonCode;
	}

	public void setReasonCode (int reasonCode)
	{
		_reasonCode = reasonCode;
	}

	private String	_description;
	public String getDescription ()
	{
		return _description;
	}

	public void setDescription (String description)
	{
		_description = description;
	}

	private String	_languageTag;
	public String getLanguageTag ()
	{
		return _languageTag;
	}

	public void setLanguageTag (String languageTag)
	{
		_languageTag = languageTag;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public ChannelOpenFailure read (final InputStream in) throws IOException
	{
		setRecipientChannel(SSHProtocol.readUint32(in));
		setReasonCode(SSHProtocol.readUint32(in));
		setDescription(SSHProtocol.readUTF8String(in));
		setLanguageTag(SSHProtocol.readASCIIString(in));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public ChannelOpenFailure decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setRecipientChannel(in.readInt());
		setReasonCode(in.readInt());
		setDescription(in.readUTF());
		setLanguageTag(in.readASCII());

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		SSHProtocol.writeUint32(out, getRecipientChannel());
		SSHProtocol.writeUint32(out, getReasonCode());
		SSHProtocol.writeStringBytes(out, false, getDescription());
		SSHProtocol.writeStringBytes(out, true, getLanguageTag());
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
		out.writeInt(getReasonCode());
		out.writeUTF(getDescription());
		out.writeASCII(getLanguageTag());
	}
}
