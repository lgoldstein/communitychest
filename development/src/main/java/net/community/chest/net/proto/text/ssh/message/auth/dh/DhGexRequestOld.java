/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.auth.dh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.community.chest.net.proto.text.ssh.SSHMsgCode;
import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;
import net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 16, 2010 12:41:52 PM
 */
public class DhGexRequestOld extends AbstractSSHMsgEncoder<DhGexRequestOld> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 946970557876349131L;
	public DhGexRequestOld ()
	{
		super(SSHMsgCode.SSH_MSG_KEX_DH_GEX_REQUEST_OLD);
	}
	/**
	 * Preferred size in bits of the group the server will send
	 * (see <A HREF="http://www.ietf.org/rfc/rfc4419.txt">RFC 4419</A> section 5).
	 */
	private int	_preferredSize;
	public int getPreferredSize ()
	{
		return _preferredSize;
	}

	public void setPreferredSize (int preferredSize)
	{
		_preferredSize = preferredSize;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public DhGexRequestOld decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setPreferredSize(in.readInt());
		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getMsgCode() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		out.writeInt(getPreferredSize());
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public DhGexRequestOld read (InputStream in) throws IOException
	{
		setPreferredSize(SSHProtocol.readUint32(in));
		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		SSHProtocol.writeUint32(out, getPreferredSize());
	}
}
