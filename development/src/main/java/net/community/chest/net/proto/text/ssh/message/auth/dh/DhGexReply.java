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
 * @since Jun 16, 2010 2:00:28 PM
 */
public class DhGexReply extends AbstractSSHMsgEncoder<DhGexReply> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -897633902705487231L;
	public DhGexReply ()
	{
		super(SSHMsgCode.SSH_MSG_KEX_DH_GEX_REPLY);
	}

	private String	_keyAndCertificates;
	public String getKeyAndCertificates ()
	{
		return _keyAndCertificates;
	}

	public void setKeyAndCertificates (String keyAndCertificates)
	{
		_keyAndCertificates = keyAndCertificates;
	}

	private SSHMultiPrecisionInteger	_fValue;
	public SSHMultiPrecisionInteger getFValue ()
	{
		return _fValue;
	}

	public void setFValue (SSHMultiPrecisionInteger fValue)
	{
		_fValue = fValue;
	}

	private String	_signature;
	public String getSignature ()
	{
		return _signature;
	}

	public void setSignature (String signature)
	{
		_signature = signature;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public DhGexReply decode (SSHInputDataDecoder in) throws IOException
	{
		setKeyAndCertificates(in.readUTF());
		setFValue(SSHProtocol.decodeMpint(in, getFValue()));
		setSignature(in.readUTF());

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getMsgCode() + ") no " + SSHOutputDataEncoder.class.getSimpleName());

		out.writeUTF(getKeyAndCertificates());
		SSHProtocol.encodeMpint(out, getFValue());
		out.writeUTF(getSignature());
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public DhGexReply read (InputStream in) throws IOException
	{
		setKeyAndCertificates(SSHProtocol.readUTF8String(in));
		setFValue(SSHProtocol.readMpint(in, getFValue()));

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		SSHProtocol.writeStringBytes(out, false, getKeyAndCertificates());
		SSHProtocol.writeMpint(out, getFValue());
		SSHProtocol.writeStringBytes(out, false, getSignature());
	}
}
