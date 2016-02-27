/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.auth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.Collection;

import net.community.chest.net.proto.text.ssh.SSHMsgCode;
import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;
import net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 2, 2009 9:38:50 AM
 */
public class UserAuthFailure extends AbstractSSHMsgEncoder<UserAuthFailure> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -933831896636947794L;
	public UserAuthFailure ()
	{
		super(SSHMsgCode.SSH_MSG_USERAUTH_FAILURE);
	}

	private Collection<String>	_allowedAuthsList;
	public Collection<String> getAllowedAuthsList ()
	{
		return _allowedAuthsList;
	}

	public void setAllowedAuthsList (Collection<String> allowedAuthsList)
	{
		_allowedAuthsList = allowedAuthsList;
	}

	private Boolean	_partialSuccess;
	public Boolean getPartialSuccess ()
	{
		return _partialSuccess;
	}

	public void setPartialSuccess (Boolean partialSuccess)
	{
		_partialSuccess = partialSuccess;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public UserAuthFailure read (final InputStream in) throws IOException
	{
		setAllowedAuthsList(SSHProtocol.readNamesList(in));
		setPartialSuccess(Boolean.valueOf(SSHProtocol.readBoolean(in)));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public UserAuthFailure decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setAllowedAuthsList(in.readNamesList());
		setPartialSuccess(Boolean.valueOf(in.readBoolean()));

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		final Boolean	ps=getPartialSuccess();
		if (null == ps)
			throw new StreamCorruptedException("write(" + getMsgCode() + ") partial success value not set");

		SSHProtocol.writeNamesList(out, getAllowedAuthsList());
		SSHProtocol.writeBoolean(out, ps.booleanValue());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getMsgCode() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		final Boolean	ps=getPartialSuccess();
		if (null == ps)
			throw new StreamCorruptedException("encode(" + getMsgCode() + ") partial success value not set");

		out.writeNamesList(getAllowedAuthsList());
		out.writeBoolean(ps.booleanValue());
	}
}
