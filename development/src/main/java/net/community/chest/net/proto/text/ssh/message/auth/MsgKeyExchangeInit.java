/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.auth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.Collection;

import net.community.chest.io.file.FileIOUtils;
import net.community.chest.net.proto.text.ssh.SSHMsgCode;
import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;
import net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 2, 2009 2:49:07 PM
 */
public class MsgKeyExchangeInit extends AbstractSSHMsgEncoder<MsgKeyExchangeInit> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4073962244078997689L;
	public MsgKeyExchangeInit ()
	{
		super(SSHMsgCode.SSH_MSG_KEXINIT);
	}

	public static final int	KEYEX_COOKIE_LEN=16;

	private byte[]	_cookie;
	public byte[] getCookie ()
	{
		return _cookie;
	}

	public void setCookie (byte ... cookie)
	{
		_cookie = cookie;
	}

	protected byte[] readCookie (final InputStream in) throws IOException
	{
		if (null == in)
			throw new IOException("readCookie(" + getMsgCode() + ") no " + InputStream.class.getSimpleName() + " instance provided");

		final byte[]	ock=getCookie(), eck;
		if ((null == ock) || (ock.length < KEYEX_COOKIE_LEN))
			eck = new byte[KEYEX_COOKIE_LEN];
		else
			eck = ock;

		FileIOUtils.readFully(in, eck, 0, KEYEX_COOKIE_LEN);
		return eck;
	}

	private Collection<String>	_kexAlgorithms;
	public Collection<String> getKexAlgorithms ()
	{
		return _kexAlgorithms;
	}

	public void setKexAlgorithms (Collection<String> kexAlgorithms)
	{
		_kexAlgorithms = kexAlgorithms;
	}

	private Collection<String>	_serverHostKeyAlgorithms;
	public Collection<String> getServerHostKeyAlgorithms ()
	{
		return _serverHostKeyAlgorithms;
	}

	public void setServerHostKeyAlgorithms (Collection<String> v)
	{
		_serverHostKeyAlgorithms = v;
	}

	private Collection<String>	_client2ServerEnc;
	public Collection<String> getClient2ServerEnc ()
	{
		return _client2ServerEnc;
	}

	public void setClient2ServerEnc (Collection<String> v)
	{
		_client2ServerEnc = v;
	}

	private Collection<String>	_server2ClientEnc;
	public Collection<String> getServer2ClientEnc ()
	{
		return _server2ClientEnc;
	}

	public void setServer2ClientEnc (Collection<String> v)
	{
		_server2ClientEnc = v;
	}

	private Collection<String>	_client2ServerMAC;
	public Collection<String> getClient2ServerMAC ()
	{
		return _client2ServerMAC;
	}

	public void setClient2ServerMAC (Collection<String> client2ServerMAC)
	{
		_client2ServerMAC = client2ServerMAC;
	}

	private Collection<String>	_server2ClientMAC;
	public Collection<String> getServer2ClientMAC ()
	{
		return _server2ClientMAC;
	}

	public void setServer2ClientMAC (Collection<String> server2ClientMAC)
	{
		_server2ClientMAC = server2ClientMAC;
	}

	private Collection<String>	_client2ServerCompress;
	public Collection<String> getClient2ServerCompress ()
	{
		return _client2ServerCompress;
	}

	public void setClient2ServerCompress (Collection<String> client2ServerCompress)
	{
		_client2ServerCompress = client2ServerCompress;
	}

	private Collection<String>	_server2ClientCompress;
	public Collection<String> getServer2ClientCompress ()
	{
		return _server2ClientCompress;
	}

	public void setServer2ClientCompress (Collection<String> server2ClientCompress)
	{
		_server2ClientCompress = server2ClientCompress;
	}

	private Collection<String>	_client2ServerLang;
	public Collection<String> getClient2ServerLang ()
	{
		return _client2ServerLang;
	}

	public void setClient2ServerLang (Collection<String> client2ServerLang)
	{
		_client2ServerLang = client2ServerLang;
	}

	private Collection<String>	_server2ClientLang;
	public Collection<String> getServer2ClientLang ()
	{
		return _server2ClientLang;
	}

	public void setServer2ClientLang (Collection<String> server2ClientLang)
	{
		_server2ClientLang = server2ClientLang;
	}

	private Boolean	_firstKexPacketFollows;
	public Boolean getFirstKexPacketFollows ()
	{
		return _firstKexPacketFollows;
	}

	public void setFirstKexPacketFollows (Boolean firstKexPacketFollows)
	{
		_firstKexPacketFollows = firstKexPacketFollows;
	}

	private int	_reservedValue;
	public int getReservedValue ()
	{
		return _reservedValue;
	}

	public void setReservedValue (int reservedValue)
	{
		_reservedValue = reservedValue;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public MsgKeyExchangeInit read (final InputStream in) throws IOException
	{
		setCookie(readCookie(in));

		setKexAlgorithms(SSHProtocol.readNamesList(in));
		setServerHostKeyAlgorithms(SSHProtocol.readNamesList(in));

		setClient2ServerEnc(SSHProtocol.readNamesList(in));
		setServer2ClientEnc(SSHProtocol.readNamesList(in));

		setClient2ServerMAC(SSHProtocol.readNamesList(in));
		setServer2ClientMAC(SSHProtocol.readNamesList(in));

		setClient2ServerCompress(SSHProtocol.readNamesList(in));
		setServer2ClientCompress(SSHProtocol.readNamesList(in));

		setClient2ServerLang(SSHProtocol.readNamesList(in));
		setServer2ClientLang(SSHProtocol.readNamesList(in));
		
		setFirstKexPacketFollows(Boolean.valueOf(SSHProtocol.readBoolean(in)));
		setReservedValue(SSHProtocol.readUint32(in));

		return this;
	}

	public MsgKeyExchangeInit (InputStream in) throws IOException
	{
		this();
		read(in);
	}

	protected byte[] decodeCookie (final SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("readCookie(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance provided");

		final byte[]	ock=getCookie(), eck;
		if ((null == ock) || (ock.length < KEYEX_COOKIE_LEN))
			eck = new byte[KEYEX_COOKIE_LEN];
		else
			eck = ock;

		in.readFully(eck, 0, KEYEX_COOKIE_LEN);
		return eck;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public MsgKeyExchangeInit decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setCookie(decodeCookie(in));

		setKexAlgorithms(in.readNamesList());
		setServerHostKeyAlgorithms(in.readNamesList());

		setClient2ServerEnc(in.readNamesList());
		setServer2ClientEnc(in.readNamesList());

		setClient2ServerMAC(in.readNamesList());
		setServer2ClientMAC(in.readNamesList());

		setClient2ServerCompress(in.readNamesList());
		setServer2ClientCompress(in.readNamesList());

		setClient2ServerLang(in.readNamesList());
		setServer2ClientLang(in.readNamesList());
		
		setFirstKexPacketFollows(Boolean.valueOf(in.readBoolean()));
		setReservedValue(in.readInt());

		return this;
	}

	protected void writeCookie (final OutputStream out) throws IOException
	{
		if (null == out)
			throw new IOException("writeCookie(" + getMsgCode() + ") no " + OutputStream.class.getSimpleName() + " instance provided");

		final byte[]	ck=getCookie();
		if ((null == ck) || (ck.length != KEYEX_COOKIE_LEN))
			throw new StreamCorruptedException("writeCookie() invalid cookie");

		out.write(ck, 0, KEYEX_COOKIE_LEN);
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		writeCookie(out);

		SSHProtocol.writeNamesList(out, getKexAlgorithms());
		SSHProtocol.writeNamesList(out, getServerHostKeyAlgorithms());

		SSHProtocol.writeNamesList(out, getClient2ServerEnc());
		SSHProtocol.writeNamesList(out, getServer2ClientEnc());

		SSHProtocol.writeNamesList(out, getClient2ServerMAC());
		SSHProtocol.writeNamesList(out, getServer2ClientMAC());

		SSHProtocol.writeNamesList(out, getClient2ServerCompress());
		SSHProtocol.writeNamesList(out, getServer2ClientCompress());

		SSHProtocol.writeNamesList(out, getClient2ServerLang());
		SSHProtocol.writeNamesList(out, getServer2ClientLang());

		final Boolean	ps=getFirstKexPacketFollows();
		if (null == ps)
			throw new StreamCorruptedException("write() 1st packet follows value");

		SSHProtocol.writeBoolean(out, ps.booleanValue());
		SSHProtocol.writeUint32(out, getReservedValue());
	}

	protected void encodeCookie (final SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encodeCookie(" + getMsgCode() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance provided");

		final byte[]	ck=getCookie();
		if ((null == ck) || (ck.length != KEYEX_COOKIE_LEN))
			throw new StreamCorruptedException("encodeCookie() invalid cookie");

		out.write(ck, 0, KEYEX_COOKIE_LEN);
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		final Boolean	ps=getFirstKexPacketFollows();
		if (null == ps)
			throw new StreamCorruptedException("encode(" + getMsgCode() + ") 1st packet follows value");

		encodeCookie(out);

		out.writeNamesList(getKexAlgorithms());
		out.writeNamesList(getServerHostKeyAlgorithms());

		out.writeNamesList(getClient2ServerEnc());
		out.writeNamesList(getServer2ClientEnc());

		out.writeNamesList(getClient2ServerMAC());
		out.writeNamesList(getServer2ClientMAC());

		out.writeNamesList(getClient2ServerCompress());
		out.writeNamesList(getServer2ClientCompress());

		out.writeNamesList(getClient2ServerLang());
		out.writeNamesList(getServer2ClientLang());

		out.writeBoolean(ps.booleanValue());
		out.writeInt(getReservedValue());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder#appendFullDescription(java.lang.StringBuilder)
	 */
	@Override
	protected StringBuilder appendFullDescription (StringBuilder sb)
	{
		return super.appendFullDescription(sb)
			.append("[1st-follows=").append(getFirstKexPacketFollows()).append(']')
			.append(";KEXALGS=").append(getKexAlgorithms())
			.append(";HOSTALGS=").append(getServerHostKeyAlgorithms())
			.append(";C2SENC=").append(getClient2ServerEnc())
			.append(";S2CENC=").append(getServer2ClientEnc())
			.append(";C2SMAC=").append(getClient2ServerMAC())
			.append(";S2CMAC=").append(getServer2ClientMAC())
			.append(";C2SCMP=").append(getClient2ServerCompress())
			.append(";S2CCMP=").append(getServer2ClientCompress())
			.append(";C2SLNG=").append(getClient2ServerLang())
			.append(";S2CLNG=").append(getServer2ClientLang())
			;
	}
}
