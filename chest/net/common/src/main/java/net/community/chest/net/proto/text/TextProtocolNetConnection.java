package net.community.chest.net.proto.text;

import java.io.IOException;

import net.community.chest.net.proto.ProtocolNetConnection;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Represents a textual protocol connection (e.g., SMTP, POP3, etc.)
 * 
 * @author Lyor G.
 * @since Jun 28, 2007 1:59:05 PM
 */
public interface TextProtocolNetConnection extends ProtocolNetConnection {
	/**
	 * Connects to specified host on port (Note: cannot be called if already connected)
	 * @param host name/IP address to which to connect to
	 * @param nPort port number on which to connect
	 * @param wl string to be used to fill in the welcome response (null == not needed)
	 * @throws IOException if connection handling error
	 */
	void connect (String host, int nPort, NetServerWelcomeLine wl) throws IOException;
	/**
	 * Connects to specified host on default protocol port (Note: cannot be called if already connected)
	 * @param host name/IP address to which to connect to
	 * @param wl string to be used to fill in the welcome response (null == not needed)
	 * @throws IOException if connection handling error
	 */
	void connect (String host, NetServerWelcomeLine wl) throws IOException;
}
