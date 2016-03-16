package net.community.chest.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

/**
 * Copyright 2007 as per GPLv2
 *
 * Provides some default implementations for the {@link NetConnection} interface
 *
 * @author Lyor G.
 * @since Jul 4, 2007 7:45:51 AM
 */
public abstract class AbstractNetConnection implements NetConnection {
    protected AbstractNetConnection ()
    {
        super();
    }
    /* NOTE !!! override either this method or the other "attach" one
     * @see net.community.chest.net.NetConnection#attach(java.net.Socket)
     */
    @Override
    public void attach (Socket sock) throws IOException
    {
        if (null == sock)
            throw new SocketException("No " + Socket.class.getName() + " instance to attach");

        attach(sock.getChannel());
    }
    /* NOTE !!! override either this method or the other "attach" one
     * @see net.community.chest.net.NetConnection#attach(java.nio.channels.SocketChannel)
     */
    @Override
    public void attach (SocketChannel channel) throws IOException
    {
        if (null == channel)
            throw new SocketException("No " + SocketChannel.class.getName() + " instance to attach");

        attach(channel.socket());
    }
    /* NOTE !!! override either this method or the "getSocket" one
     * @see net.community.chest.net.NetConnection#getChannel()
     */
    @Override
    public SocketChannel getChannel ()
    {
        final Socket    sock=getSocket();
        return (null == sock) ? null : sock.getChannel();
    }
    /* NOTE !!! override either this method or the "getChannel" one
     * @see net.community.chest.net.NetConnection#getSocket()
     */
    @Override
    public Socket getSocket ()
    {
        final SocketChannel    channel=getChannel();
        return (null == channel) ? null : channel.socket();
    }
    /* NOTE !!! override either this method or the "detachSocket" one
     * @see net.community.chest.net.NetConnection#detachChannel()
     */
    @Override
    public SocketChannel detachChannel () throws IOException
    {
        final Socket    sock=detachSocket();
        return (null == sock) ? null : sock.getChannel();
    }
    /* NOTE !!! override either this method or the "detachChannel" one
     * @see net.community.chest.net.NetConnection#detachSocket()
     */
    @Override
    public Socket detachSocket () throws IOException
    {
        final SocketChannel    channel=detachChannel();
        return (null == channel) ? null : channel.socket();
    }
    /*
     * @see java.io.Closeable#close()
     */
    @Override
    public void close () throws IOException
    {
        IOException    exc=null;
        if (isOpen())
        {
            try
            {
                flush();
            }
            catch(IOException ioe)
            {
                exc = ioe;
            }
        }

        // see http://java.sun.com/j2se/1.5.0/docs/guide/net/articles/connection_release.html
        final Socket    s=detachSocket();
        if (s != null)
        {
            try
            {
                s.shutdownOutput();
            }
            catch(IOException ioe)
            {
                if (null == exc)
                    exc = ioe;
            }

            try(InputStream    in=s.getInputStream()) {
                // no more data is expected, but consume it anyway
                for (int    v=in.read(), numRead=0; v >= 0; numRead++, v=in.read())
                {
                    if (numRead < 0)
                        numRead = 0;
                }
            }
            catch(IOException e)
            {
                    // ignored
            }

            try
            {
                s.close();
            }
            catch(IOException ioe)
            {
                if (null == exc)
                    exc = ioe;
            }
        }

        if (exc != null)
            throw exc;
    }
}
