package net.community.chest.net.proto;

import java.io.IOException;

import net.community.chest.net.NetConnection;

/**
 * Copyright 2007 as per GPLv2
 *
 * Represents a "well-known" protocol network connection
 *
 * @author Lyor G.
 * @since Jun 28, 2007 2:02:47 PM
 */
public interface ProtocolNetConnection extends NetConnection {
    /**
     * @return default protocol port
     */
    int getDefaultPort ();
    /**
     * Connects to specified host on default protocol port
     * @param host name/IP address to which to connect to
     * @see #getDefaultPort()
     * @throws IOException if connection handling error
     */
    void connect (String host) throws IOException;
}
