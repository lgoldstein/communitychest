/*
 * 
 */
package net.community.chest.net.proto.text.ssh;

import java.io.IOException;

import net.community.chest.net.TextNetConnection;
import net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder;


/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>A helper interface used in various stages of the SSH protocol</P>
 * @author Lyor G.
 * @since Jul 2, 2009 10:17:16 AM
 */
public interface SSHProtocolHandler {
	/**
	 * Called during the connection phase if any data received from the
	 * server prior to the identification line
	 * @param text The received data
	 * @throws IOException if handling failure
	 */
	void handleInitialBannerData (String text) throws IOException;
	/**
	 * Called during the connection phase to inform about the reported
	 * remote identification according to RFC 4253 
	 * @param ident The received identification string
	 * @throws IOException if handling failure
	 */
	void handleInitialIdentificationData (String ident) throws IOException;
	/**
	 * Called during the connection phase to allow sending some initial
	 * banner data according to RFC 4253 
	 * @param conn The established connection
	 * @throws IOException If failed to send
	 */
	void sendInitialBannerData (TextNetConnection conn) throws IOException;
	/**
	 * Called during initial handshake to allow sending an SSH identification
	 * string value according to RFC 4253
	 * @return The characters to use to send the identification
	 * @throws IOException if handling failure
	 */
	char[] getIdentificationCharacters () throws IOException;
	/**
	 * Called after a new packet has been read but before it has been processed
	 * @param hdr The read {@link SSHPacketHeader} instance
	 * @return The updated {@link SSHPacketHeader} - may be same as input. If
	 * null then no more processing.
	 * @throws IOException If failed to process the header
	 */
	SSHPacketHeader preProcessPacket (SSHPacketHeader hdr) throws IOException;
	/**
	 * Called before a decoded message is handled
	 * @param msg The decoded message data
	 * @return TRUE if to continue processing the message, FALSE=ignore and
	 * return (handled by the handler)
	 * @throws IOException If failed to process the message
	 */
	boolean preProcessDecodedMessage (AbstractSSHMsgEncoder<?> msg) throws IOException;
}
