/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.channel.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 12, 2009 12:25:36 PM
 */
public class FlowControlChannelRequest extends AbstractChannelRequestSpecificTypeData<FlowControlChannelRequest> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8386404733485012602L;
	public static final String	REQ_TYPE="xon-xoff";
	public FlowControlChannelRequest ()
	{
		super(REQ_TYPE);
	}

	private Boolean	_clientCanDo;
	public Boolean getClientCanDo ()
	{
		return _clientCanDo;
	}

	public void setClientCanDo (Boolean clientCanDo)
	{
		_clientCanDo = clientCanDo;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public FlowControlChannelRequest read (final InputStream in) throws IOException
	{
		setClientCanDo(Boolean.valueOf(SSHProtocol.readBoolean(in)));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public FlowControlChannelRequest decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getRequestType() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setClientCanDo(Boolean.valueOf(in.readBoolean()));
		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		final Boolean	rr=getClientCanDo();
		if (null == rr)
			throw new StreamCorruptedException("write(" + getRequestType() + ") client-can-do not set");

		SSHProtocol.writeBoolean(out, rr.booleanValue());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getRequestType() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		final Boolean	rr=getClientCanDo();
		if (null == rr)
			throw new StreamCorruptedException("write(" + getRequestType() + ") client-can-do not set");

		out.writeBoolean(rr.booleanValue());
	}
}
