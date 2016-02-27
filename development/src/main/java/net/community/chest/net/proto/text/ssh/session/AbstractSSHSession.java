/*
 * 
 */
package net.community.chest.net.proto.text.ssh.session;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import net.community.chest.io.file.FileIOUtils;
import net.community.chest.io.input.DynamicByteArrayInputStream;
import net.community.chest.lang.StringUtil;
import net.community.chest.net.BufferedTextSocket;
import net.community.chest.net.proto.text.NetServerWelcomeLine;
import net.community.chest.net.proto.text.ssh.SSHAccessor;
import net.community.chest.net.proto.text.ssh.SSHMsgCode;
import net.community.chest.net.proto.text.ssh.SSHPacketHeader;
import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.SSHProtocolHandler;
import net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder;
import net.community.chest.net.proto.text.ssh.message.auth.MsgKeyExchangeInit;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 2, 2009 10:20:13 AM
 */
public abstract class AbstractSSHSession extends BufferedTextSocket implements SSHAccessor {
	protected AbstractSSHSession ()
	{
		super();
	}

	protected AbstractSSHSession (int readBufSizeVal, int writeBufSizeVal)
	{
		super(readBufSizeVal, writeBufSizeVal);
	}

	protected AbstractSSHSession (Socket sock, int readBufSizeVal, int writeBufSizeVal) throws IOException
	{
		super(sock, readBufSizeVal, writeBufSizeVal);
	}

	protected AbstractSSHSession (Socket sock) throws IOException
	{
		super(sock);
	}

	protected AbstractSSHSession (SocketChannel sock, int readBufSizeVal, int writeBufSizeVal) throws IOException
	{
		super(sock, readBufSizeVal, writeBufSizeVal);
	}

	protected AbstractSSHSession (SocketChannel sock) throws IOException
	{
		super(sock);
	}

	private SSHProtocolHandler	_hndlr;
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHAccessor#getProtocolHandler()
	 */
	@Override
	public SSHProtocolHandler getProtocolHandler ()
	{
		return _hndlr;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHAccessor#setProtocolHandler(net.community.chest.net.proto.text.ssh.SSHProtocolHandler)
	 */
	@Override
	public void setProtocolHandler (SSHProtocolHandler hndlr)
	{
		if (_hndlr != hndlr)
			_hndlr = hndlr;
	}

	private byte[]	_readPacketData	/* =null */;
	// returns a byte[] at least of requested len (maybe more)
	protected byte[] getReadPacketDataBuffer (int len) throws IOException
	{
		if (len > SSHProtocol.MAX_PACKET_SIZE)
			throw new StreamCorruptedException("getReadPacketDataBuffer(" + len + ") exceeds max. allowed: " + SSHProtocol.MAX_PACKET_SIZE);

		if ((null == _readPacketData) || (_readPacketData.length < len))
			_readPacketData = new byte[len];
		return _readPacketData;
	}

	protected SSHPacketHeader readPacket (final SSHPacketHeader hdr)
		throws IOException
	{
		final InputStream		in=getInputStream();
		final SSHPacketHeader	h=hdr.read(in);
		final int				dLen=h.getPayloadLength();
		final byte[]			dData=getReadPacketDataBuffer(dLen);
		FileIOUtils.readFully(in, dData, 0, dLen);
		hdr.setPayloadData(dData);

		FileIOUtils.skipFully(in, h.getPadLength());

		final SSHProtocolHandler	hndlr=getProtocolHandler();
		final SSHPacketHeader		retHdr=
			(null == hndlr) ? hdr : hndlr.preProcessPacket(hdr);
		if (retHdr == hdr)
			return hdr;

		return retHdr;	// debug breakpoint
	}

	private DynamicByteArrayInputStream	_pktStream;
	private static final byte[]	EMPTY_DATA=new byte[0];
	protected InputStream getPacketDataInputStream (SSHPacketHeader hdr)
	{
		final int		pdLen=(null == hdr) ? 0 : hdr.getPayloadLength();
		final byte[]	pdData=(null == hdr) ? EMPTY_DATA : hdr.getPayloadData();
		if (null == _pktStream)
			_pktStream = new DynamicByteArrayInputStream(pdData, 0, pdLen);
		else
			_pktStream.setData(pdData, 0, pdLen);
		return _pktStream;
	}

	protected <M extends AbstractSSHMsgEncoder<M>> M readMessage (
			final M message, final InputStream pki)
		throws IOException
	{
		final M						msg=message.read(pki);
		final SSHProtocolHandler	hndlr=getProtocolHandler();
		if ((hndlr != null) && (!hndlr.preProcessDecodedMessage(msg)))
			return null;

		return msg;
	}

	public void doKeyExchange (final InputStream pki) throws IOException
	{
		final MsgKeyExchangeInit	kex=readMessage(new MsgKeyExchangeInit(), pki);
		if (null == kex)	// assume handler did something to it
			return;
	}

	protected void doPacketLoop () throws IOException
	{
		for (long	pkIndex=0L; ; pkIndex++)
		{
			final SSHPacketHeader	hdr=readPacket(new SSHPacketHeader());
			if (null == hdr)	// assume handler did something to it
				continue;

			final InputStream		pki=getPacketDataInputStream(hdr);
			final int				opCode=pki.read();
			if ((-1) == opCode)
				throw new EOFException("doPacketLoop() out-of-data after " + pkIndex + " packets");

			handlePacket(hdr, opCode, pki);
		}
	}

	protected void handlePacket (final SSHPacketHeader	hdr, final int opCode, final InputStream pki)
		throws IOException
	{
		if (opCode == SSHMsgCode.SSH_MSG_KEXINIT.getCodeValue())
			doKeyExchange(pki);
		else
			handleUnknownOpcode(hdr, opCode, pki);
	}

	protected void handleUnknownOpcode (final SSHPacketHeader	hdr, final int opCode, final InputStream pki)
		throws IOException
	{
		if (pki == null)
			throw new EOFException("handleUnknownOpcode(" + hdr + ")[opcode=" + opCode + "] - no data stream");

		throw new StreamCorruptedException("handleUnknownOpcode(" + hdr + ") unknown opcode: " + opCode);
	}
	/*
	 * @see net.community.chest.net.proto.ProtocolNetConnection#getDefaultPort()
	 */
	@Override
	public final int getDefaultPort ()
	{
		return SSHProtocol.IPPORT_SSH;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHAccessor#connect(java.lang.String, int, net.community.chest.net.proto.text.NetServerWelcomeLine, net.community.chest.net.proto.text.ssh.SSHProtocolHandler)
	 */
	@Override
	public void connect (String host, int nPort, NetServerWelcomeLine wl, SSHProtocolHandler hndlr)
		throws IOException
	{
		if (isOpen())
			throw new StreamCorruptedException("connect(" + host + "@" + nPort + ") already connected");

		super.connect(host, nPort);

		try
		{
			for (String	wlString=readLine(); ; )
			{
				if (StringUtil.startsWith(wlString, SSHProtocol.SSH_PREFIX, true, true))
				{
					if (wl != null)
						wl.setLine(wlString);
					if (hndlr != null)
						hndlr.handleInitialIdentificationData(wlString);
					// TODO remember/process this string since it will be needed for encryption 
					break;
				}

				if (hndlr != null)
					hndlr.handleInitialBannerData(wlString);
			}

			// allow the user to send some initial banner data
			if (hndlr != null)
				hndlr.sendInitialBannerData(this);

			final char[]	identChars=
				(null == hndlr) ? SSHProtocol.SSH_IDENTChars : hndlr.getIdentificationCharacters();
			final int		wLen=writeln(identChars, true);
			if (wLen <= identChars.length)
				throw new StreamCorruptedException("connect(" + host + "@" + nPort + ") mismatched written identification data: got=" + wLen + "/expected>" + identChars.length);

			doPacketLoop();
		}
		catch(IOException ioe)
		{
			close();
			throw ioe;
		}

		setProtocolHandler(hndlr);
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHAccessor#connect(java.lang.String, int, net.community.chest.net.proto.text.ssh.SSHProtocolHandler)
	 */
	@Override
	public void connect (String host, int nPort, SSHProtocolHandler hndlr) throws IOException
	{
		connect(host, nPort, null, hndlr);
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHAccessor#connect(java.lang.String, net.community.chest.net.proto.text.NetServerWelcomeLine, net.community.chest.net.proto.text.ssh.SSHProtocolHandler)
	 */
	@Override
	public void connect (String host, NetServerWelcomeLine wl, SSHProtocolHandler hndlr) throws IOException
	{
		connect(host, getDefaultPort(), wl, hndlr);
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHAccessor#connect(java.lang.String, net.community.chest.net.proto.text.ssh.SSHProtocolHandler)
	 */
	@Override
	public void connect (String host, SSHProtocolHandler hndlr) throws IOException
	{
		connect(host, getDefaultPort(), hndlr);
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolNetConnection#connect(java.lang.String, int, net.community.chest.net.proto.text.NetServerWelcomeLine)
	 */
	@Override
	public void connect (String host, int nPort, NetServerWelcomeLine wl) throws IOException
	{
		connect(host, nPort, wl, getProtocolHandler());
	}
	/*
	 * @see net.community.chest.net.proto.ProtocolNetConnection#connect(java.lang.String)
	 */
	@Override
	public void connect (String host) throws IOException
	{
		connect(host, getDefaultPort(), getProtocolHandler());
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolNetConnection#connect(java.lang.String, net.community.chest.net.proto.text.NetServerWelcomeLine)
	 */
	@Override
	public void connect (String host, NetServerWelcomeLine wl) throws IOException
	{
		connect(host, getDefaultPort(), wl);
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHAccessor#login(java.lang.String, java.lang.String)
	 */
	@Override
	public void login (String username, String password) throws IOException
	{
		throw new StreamCorruptedException("login(" + username + ") N/A");
	}
}
