/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.auth.dh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.community.chest.net.proto.text.ssh.SSHMsgCode;
import net.community.chest.net.proto.text.ssh.SSHMultiPrecisionInteger;
import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;
import net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * <P>See <A HREF="http://www.ietf.org/rfc/rfc4419.txt">RFC4419</A> section 3</P>
 * 
 * @author Lyor G.
 * @since Jun 16, 2010 1:57:03 PM
 */
public class DhGexInit extends AbstractSSHMsgEncoder<DhGexInit> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 178280143794248481L;
	public DhGexInit ()
	{
		super(SSHMsgCode.SSH_MSG_KEX_DH_GEX_INIT);
	}

	private SSHMultiPrecisionInteger	_eValue;
	public SSHMultiPrecisionInteger getEValue ()
	{
		return _eValue;
	}

	public void setEValue (SSHMultiPrecisionInteger eValue)
	{
		_eValue = eValue;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public DhGexInit decode (SSHInputDataDecoder in) throws IOException
	{
		setEValue(SSHProtocol.decodeMpint(in, getEValue()));
		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		SSHProtocol.encodeMpint(out, getEValue());
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public DhGexInit read (InputStream in) throws IOException
	{
		setEValue(SSHProtocol.readMpint(in, getEValue()));
		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		SSHProtocol.writeMpint(out, getEValue());
	}
}
