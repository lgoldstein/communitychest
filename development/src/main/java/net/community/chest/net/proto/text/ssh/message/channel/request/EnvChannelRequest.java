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
 * @since Jul 12, 2009 12:13:14 PM
 */
public class EnvChannelRequest extends AbstractChannelRequestSpecificTypeData<EnvChannelRequest> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5561260865137371738L;
	public static final String	REQ_TYPE="env";
	public EnvChannelRequest ()
	{
		super(REQ_TYPE);
	}

	private String	_varName;
	public String getVarName ()
	{
		return _varName;
	}

	public void setVarName (String varName)
	{
		_varName = varName;
	}

	private String	_varValue;
	public String getVarValue ()
	{
		return _varValue;
	}

	public void setVarValue (String varValue)
	{
		_varValue = varValue;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public EnvChannelRequest read (final InputStream in) throws IOException
	{
		setVarName(SSHProtocol.readASCIIString(in));
		setVarValue(SSHProtocol.readASCIIString(in));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public EnvChannelRequest decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getRequestType() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setVarName(in.readASCII());
		setVarValue(in.readASCII());

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		SSHProtocol.writeStringBytes(out, true, getVarName());
		SSHProtocol.writeStringBytes(out, true, getVarValue());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getRequestType() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		out.writeASCII(getVarName());
		out.writeASCII(getVarValue());
	}
}
