/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

import net.community.chest.net.proto.text.ssh.SSHMsgCode;
import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 12, 2009 10:49:21 AM
 */
public class GlobalRequest extends AbstractSSHMsgEncoder<GlobalRequest> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6904657091019382420L;
	public GlobalRequest ()
	{
		super(SSHMsgCode.SSH_MSG_GLOBAL_REQUEST);
	}

	private String	_requestName;
	public String getRequestName ()
	{
		return _requestName;
	}

	public void setRequestName (String requestName)
	{
		_requestName = requestName;
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
	public GlobalRequest read (final InputStream in) throws IOException
	{
		setRequestName(SSHProtocol.readASCIIString(in));
		setReplyRequested(Boolean.valueOf(SSHProtocol.readBoolean(in)));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public GlobalRequest decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setRequestName(in.readASCII());
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

		SSHProtocol.writeStringBytes(out, true, getRequestName());
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

		out.writeASCII(getRequestName());
		out.writeBoolean(rr.booleanValue());
	}
}
