/*
 * 
 */
package net.community.chest.net.proto.text.ssh.io;

import java.io.IOException;

import net.community.chest.net.TextNetConnection;
import net.community.chest.net.proto.text.ssh.SSHPacketHeader;
import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.SSHProtocolHandler;
import net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 16, 2010 2:42:33 PM
 */
public class DefaultSSHProtocolHandler implements SSHProtocolHandler {
	public DefaultSSHProtocolHandler ()
	{
		super();
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHProtocolHandler#getIdentificationCharacters()
	 */
	@Override
	public char[] getIdentificationCharacters () throws IOException
	{
		return SSHProtocol.SSH_IDENTChars;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHProtocolHandler#handleInitialBannerData(java.lang.String)
	 */
	@Override
	public void handleInitialBannerData (String text) throws IOException
	{
		if ((null == text) || (text.length() <= 0))
			return;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHProtocolHandler#handleInitialIdentificationData(java.lang.String)
	 */
	@Override
	public void handleInitialIdentificationData (String ident) throws IOException
	{
		if ((null == ident) || (ident.length() <= 0))
			return;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHProtocolHandler#preProcessDecodedMessage(net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder)
	 */
	@Override
	public boolean preProcessDecodedMessage (AbstractSSHMsgEncoder<?> msg)
			throws IOException
	{
		if (null == msg)
			return false;

		return true;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHProtocolHandler#preProcessPacket(net.community.chest.net.proto.text.ssh.SSHPacketHeader)
	 */
	@Override
	public SSHPacketHeader preProcessPacket (SSHPacketHeader hdr)
			throws IOException
	{
		return hdr;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHProtocolHandler#sendInitialBannerData(net.community.chest.net.TextNetConnection)
	 */
	@Override
	public void sendInitialBannerData (TextNetConnection conn) throws IOException
	{
		if (null == conn)
			return;
	}

}
