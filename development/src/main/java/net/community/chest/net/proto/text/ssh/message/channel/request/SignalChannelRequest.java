/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.channel.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 12, 2009 12:30:00 PM
 */
public class SignalChannelRequest extends AbstractChannelRequestSpecificTypeData<SignalChannelRequest> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1723887410091803958L;
	public static final String	REQ_TYPE="signal";
	public SignalChannelRequest ()
	{
		super(REQ_TYPE);
	}

	private String	_signalName;
	public String getSignalName()
	{
		return _signalName;
	}

	public void setSignalName(String v)
	{
		_signalName = v;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public SignalChannelRequest read (final InputStream in) throws IOException
	{
		setSignalName(SSHProtocol.readASCIIString(in));
		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public SignalChannelRequest decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getRequestType() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setSignalName(in.readASCII());
		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		SSHProtocol.writeStringBytes(out, true, getSignalName());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getRequestType() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		out.writeASCII(getSignalName());
	}
}
