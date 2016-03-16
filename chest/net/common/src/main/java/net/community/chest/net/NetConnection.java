package net.community.chest.net;

import java.io.Flushable;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;

/**
 * Copyright 2007 as per GPLv2
 *
 * Represents a network connection
 *
 * @author Lyor G.
 * @since Jun 28, 2007 1:38:31 PM
 */
public interface NetConnection extends Flushable, Channel {
    /**
     * Connects to the specified host on the specified port number
     * @param host host name/IP address to which to connect
     * @param port port number to connect on
     * @see #setReadTimeout(int timeMillis)
     * @throws IOException if connection handling error
     */
    void connect (final String host, final int port) throws IOException;
    /**
     * Attaches the object to the given socket channel (if not already attached).
     * This method is intended mainly for socket channels obtained via a SERVER
     * socket channel - i.e., WAITING for incoming connection rather than initiating one.
     * @param channel channel to be attached
     * @throws IOException if connection handling error
     * @see #attach(Socket sock)
     */
    void attach (SocketChannel channel) throws IOException;
    /**
     * Attaches the object to the given socket channel (if not already attached).
     * This method is intended mainly for socket channels obtained via a SERVER
     * socket channel - i.e., WAITING for incoming connection rather than initiating one.
     * @param sock socket to be attached
     * @throws IOException if connection handling error
     * @see #attach(SocketChannel channel)
     */
    void attach (Socket sock) throws IOException;
    /**
     * @return underlying socket channel (null if none set)
     */
    SocketChannel getChannel ();
    /**
     * @return underlying socket object (null if none set)
     */
    Socket getSocket ();
    /**
     * Detaches the underlying socket channel (if any) without closing it (!)
     * @return underlying socket channel (null if none set)
     * @throws IOException if internal detach error
     */
    SocketChannel detachChannel () throws IOException;
    /**
     * Detaches the underlying socket (if any) without closing it (!)
     * @return underlying socket (null if none set)
     * @throws IOException if internal detach error
     */
    Socket detachSocket () throws IOException;
    /**
     * Specifies the read timeout (msec.) on input - Note: the function may be called BEFORE
     * connecting to the remote host.
     * @param timeMillis timeout for waiting on new input - 0 == INFINITE, (<0) illegal
     * @throws IOException if cannot set the specified value
     * @see #connect(String host, int port)
     * @see #getReadTimeout()
     */
    void setReadTimeout (final int timeMillis) throws IOException;
    /**
     * @return current read timeout (msec.) value
     * @see #setReadTimeout(int timeMillis)
     */
    int getReadTimeout ();
    /**
     * @return Remote host name/address to which currently connected (or null/empty) - the actual
     * return value depends on the initial argument supplied to the <I>"connect"</I> method - the
     * return value of this function is EXACTLY the one supplied to the <I>"connect"</I> method.
     * @see #connect(String host, int port)
     */
    String getRemoteHostName ();
    /**
     * @return remote IP address to which currently connected (or null/empty)
     * @see #getRemoteAddress()
     * @see #getRemoteHostName()
     */
    String getRemoteAddress ();
    /**
     * @return remote port to which currently connected (or <=0 if error/undefined)
     * @see #connect(String host, int port)
     */
    int getRemotePort ();
    /**
     * @return local port to which the connection is bound (or <=0 if error/undefined)
     */
    int getLocalPort ();
}
