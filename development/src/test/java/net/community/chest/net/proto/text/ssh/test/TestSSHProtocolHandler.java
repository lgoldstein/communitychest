/*
 * 
 */
package net.community.chest.net.proto.text.ssh.test;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamCorruptedException;

import net.community.chest.io.ApplicationIOUtils;
import net.community.chest.net.TextNetConnection;
import net.community.chest.net.proto.text.ssh.SSHMsgCode;
import net.community.chest.net.proto.text.ssh.SSHPacketHeader;
import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.SSHProtocolHandler;
import net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 16, 2010 11:45:51 AM
 */
public class TestSSHProtocolHandler extends ApplicationIOUtils implements SSHProtocolHandler {
	private final PrintStream		_out;
	private final BufferedReader	_in;
	public TestSSHProtocolHandler (final PrintStream out, final BufferedReader in)
	{
		_out = out;
		_in = in;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHProtocolHandler#getIdentificationCharacters()
	 */
	@Override
	public char[] getIdentificationCharacters () throws IOException
	{
		final String	reply=getval(_out, _in, "getIdentificationCharacters (ENTER=" + SSHProtocol.SSH_IDENT + ")/(Q)uit");
		if ((null == reply) || (reply.length() <= 0))
			return SSHProtocol.SSH_IDENTChars;
		if (isQuit(reply))
			throw new EOFException("Terminated by user request");

		return reply.toCharArray();
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHProtocolHandler#sendInitialBannerData(net.community.chest.net.TextNetConnection)
	 */
	@Override
	public void sendInitialBannerData (TextNetConnection conn)
			throws IOException
	{
		// do nothing
	}

	public static final String	TRACE_DATA_PREFIX="==> ";
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHProtocolHandler#handleInitialBannerData(java.lang.String)
	 */
	@Override
	public void handleInitialBannerData (String text) throws IOException
	{
		_out.append(TRACE_DATA_PREFIX)
			   .append("handleInitialBannerData(")
			   .append(text)
			   .append(')')
		   .println()
		   ;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHProtocolHandler#handleInitialIdentificationData(java.lang.String)
	 */
	@Override
	public void handleInitialIdentificationData (String ident) throws IOException
	{
		_out.append(TRACE_DATA_PREFIX)
				.append("handleInitialIdentificationData(")
		   		.append(ident)
		   		.append(')')
		   .println()
		   ;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHProtocolHandler#preProcessPacket(net.community.chest.net.proto.text.ssh.SSHPacketHeader)
	 */
	@Override
	public SSHPacketHeader preProcessPacket (SSHPacketHeader hdr) throws IOException
	{
		final int	pdLen=(null == hdr) ? 0 : hdr.getPayloadLength();
		if (pdLen <= 0)
			throw new StreamCorruptedException("preProcessPacket() no header data provided");

		final byte[]		data=hdr.getPayloadData();
		final int			op=data[0];
		final SSHMsgCode	c=SSHMsgCode.fromReasonCode(op);
		_out.append(TRACE_DATA_PREFIX)
				.append("preProcessPacket(")
				.append((null == c) ? String.valueOf(op) : c.name())
				.append(") len=")
				.append(String.valueOf(pdLen))
			.println()
			;
		return hdr;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHProtocolHandler#preProcessDecodedMessage(net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder)
	 */
	@Override
	public boolean preProcessDecodedMessage (AbstractSSHMsgEncoder<?> msg) throws IOException
	{
		_out.append(TRACE_DATA_PREFIX)
		   		.append("preProcessDecodedMessage(")
		   		.append(msg.getMsgCode().name())
		   		.append(')')
		   	.println()
		   	;

		final String	desc=msg.toFullDescription();
		if ((desc != null) && (desc.length() > 0))
			_out.println(desc.replace(';', '\n'));

		return true;
	}
}
