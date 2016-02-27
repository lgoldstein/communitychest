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
 * @since Jun 16, 2010 1:39:50 PM
 */
public class DhGexGroup extends AbstractSSHMsgEncoder<DhGexGroup> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1901501408147199841L;
	public DhGexGroup ()
	{
		super(SSHMsgCode.SSH_MSG_KEX_DH_GEX_GROUP);
	}
	/**
	 * Safe prime value from server
	 */
	private SSHMultiPrecisionInteger	_safePrime;
	public SSHMultiPrecisionInteger getSafePrime ()
	{
		return _safePrime;
	}

	public void setSafePrime (SSHMultiPrecisionInteger safePrime)
	{
		_safePrime = safePrime;
	}
	/**
	 * Generator for subgroup in GF({@link #getSafePrime()})
	 */
	private SSHMultiPrecisionInteger	_subGroupGenerator;
	public SSHMultiPrecisionInteger getSubGroupGenerator ()
	{
		return _subGroupGenerator;
	}

	public void setSubGroupGenerator (SSHMultiPrecisionInteger subGroupGenerator)
	{
		_subGroupGenerator = subGroupGenerator;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public DhGexGroup read (InputStream in) throws IOException
	{
		setSafePrime(SSHProtocol.readMpint(in, getSafePrime()));
		setSubGroupGenerator(SSHProtocol.readMpint(in, getSubGroupGenerator()));

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		SSHProtocol.writeMpint(out, getSafePrime());
		SSHProtocol.writeMpint(out, getSubGroupGenerator());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public DhGexGroup decode (SSHInputDataDecoder in) throws IOException
	{
		setSafePrime(SSHProtocol.decodeMpint(in, getSafePrime()));
		setSubGroupGenerator(SSHProtocol.decodeMpint(in, getSubGroupGenerator()));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		SSHProtocol.encodeMpint(out, getSafePrime());
		SSHProtocol.encodeMpint(out, getSubGroupGenerator());
	}
}
