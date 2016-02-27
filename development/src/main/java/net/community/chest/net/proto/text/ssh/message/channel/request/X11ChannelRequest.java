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
 * @since Jul 12, 2009 12:04:22 PM
 */
public class X11ChannelRequest extends AbstractChannelRequestSpecificTypeData<X11ChannelRequest> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5397159386302056030L;
	public static final String	REQ_TYPE="x11-req";
	public X11ChannelRequest ()
	{
		super(REQ_TYPE);
	}
	// null=not initialized
	private Boolean	_singleConnection;
	public Boolean getSingleConnection ()
	{
		return _singleConnection;
	}

	public void setSingleConnection (Boolean singleConnection)
	{
		_singleConnection = singleConnection;
	}

	private String	_x11AuthProtocol;
	public String getX11AuthProtocol ()
	{
		return _x11AuthProtocol;
	}

	public void setX11AuthProtocol (String x11AuthProtocol)
	{
		_x11AuthProtocol = x11AuthProtocol;
	}

	private byte[]	_x11AuthCookie;
	public byte[] getX11AuthCookie ()
	{
		return _x11AuthCookie;
	}

	public void setX11AuthCookie (byte[] x11AuthCookie)
	{
		_x11AuthCookie = x11AuthCookie;
	}

	private int	_x11ScreenNumber;
	public int getX11ScreenNumber ()
	{
		return _x11ScreenNumber;
	}

	public void setX11ScreenNumber (int x11ScreenNumber)
	{
		_x11ScreenNumber = x11ScreenNumber;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public X11ChannelRequest read (final InputStream in) throws IOException
	{
		setSingleConnection(Boolean.valueOf(SSHProtocol.readBoolean(in)));
		setX11AuthProtocol(SSHProtocol.readASCIIString(in));
		setX11AuthCookie(SSHProtocol.readBlobData(in));
		setX11ScreenNumber(SSHProtocol.readUint32(in));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public X11ChannelRequest decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getRequestType() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setSingleConnection(Boolean.valueOf(in.readBoolean()));
		setX11AuthProtocol(in.readASCII());
		setX11AuthCookie(in.readBlob());
		setX11ScreenNumber(in.readInt());

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		final Boolean	rr=getSingleConnection();
		if (null == rr)
			throw new StreamCorruptedException("write(" + getRequestType() + ") - single connection value not set");

		SSHProtocol.writeBoolean(out, rr.booleanValue());
		SSHProtocol.writeStringBytes(out, true, getX11AuthProtocol());
		SSHProtocol.writeStringBytes(out, getX11AuthCookie());
		SSHProtocol.writeUint32(out, getX11ScreenNumber());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getRequestType() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		final Boolean	rr=getSingleConnection();
		if (null == rr)
			throw new StreamCorruptedException("write(" + getRequestType() + ") - single connection value not set");

		out.writeBoolean(rr.booleanValue());
		out.writeASCII(getX11AuthProtocol());
		out.write(getX11AuthCookie());
		out.writeInt(getX11ScreenNumber());
	}
}
