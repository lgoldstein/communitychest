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
 * @since Jun 16, 2010 12:48:14 PM
 */
public class DhGexRequest extends AbstractSSHMsgEncoder<DhGexRequest> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2029130244845768417L;
	public DhGexRequest ()
	{
		super(SSHMsgCode.SSH_MSG_KEX_DH_GEX_REQUEST);
	}
	/**
	 * Minimal size in bits of an acceptable group
	 * (see <A HREF="http://www.ietf.org/rfc/rfc4419.txt">RFC 4419</A> section 3).
	 */
	private int	_minSize;
	public int getMinSize ()
	{
		return _minSize;
	}

	public void setMinSize (int minSize)
	{
		_minSize = minSize;
	}
	/**
	 * Preferred size in bits of the group the server will send
	 * (see <A HREF="http://www.ietf.org/rfc/rfc4419.txt">RFC 4419</A> section 3).
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
	/**
	 * Maximal size in bits of an acceptable group
	 * (see <A HREF="http://www.ietf.org/rfc/rfc4419.txt">RFC 4419</A> section 3).
	 */
	private int	_maxSize;
	public int getMaxSize ()
	{
		return _maxSize;
	}

	public void setMaxSize (int maxSize)
	{
		_maxSize = maxSize;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public DhGexRequest decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setMinSize(in.readInt());
		setPreferredSize(in.readInt());
		setMaxSize(in.readInt());
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

		out.writeInt(getMinSize());
		out.writeInt(getPreferredSize());
		out.writeInt(getMaxSize());
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public DhGexRequest read (InputStream in) throws IOException
	{
		setMinSize(SSHProtocol.readUint32(in));
		setPreferredSize(SSHProtocol.readUint32(in));
		setMaxSize(SSHProtocol.readUint32(in));
		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		SSHProtocol.writeUint32(out, getMinSize());
		SSHProtocol.writeUint32(out, getPreferredSize());
		SSHProtocol.writeUint32(out, getMaxSize());
	}
}
