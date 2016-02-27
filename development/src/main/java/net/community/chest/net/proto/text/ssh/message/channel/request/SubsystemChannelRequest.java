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
 * @since Jul 12, 2009 12:21:06 PM
 */
public class SubsystemChannelRequest extends AbstractChannelRequestSpecificTypeData<SubsystemChannelRequest> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -382331764143935620L;
	public static final String	REQ_TYPE="subsystem";
	public SubsystemChannelRequest ()
	{
		super(REQ_TYPE);
	}

	private String	_subsystemName;
	public String getSubsystemName ()
	{
		return _subsystemName;
	}

	public void setSubsystemName (String subsystemName)
	{
		_subsystemName = subsystemName;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public SubsystemChannelRequest read (final InputStream in) throws IOException
	{
		setSubsystemName(SSHProtocol.readASCIIString(in));
		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public SubsystemChannelRequest decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getRequestType() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setSubsystemName(in.readASCII());
		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		SSHProtocol.writeStringBytes(out, true, getSubsystemName());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getRequestType() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		out.writeASCII(getSubsystemName());
	}
}
