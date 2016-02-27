package net.community.chest.net.proto.text;

import java.io.EOFException;
import java.io.IOException;

import net.community.chest.net.TextNetConnection;
import net.community.chest.net.proto.AbstractProtocolNetConnection;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Helper class for {@link TextProtocolNetConnection} implementation
 * 
 * @author Lyor G.
 * @since Jul 4, 2007 9:12:29 AM
 */
public abstract class AbstractTextProtocolNetConnection extends
		AbstractProtocolNetConnection implements TextProtocolNetConnection {
	protected AbstractTextProtocolNetConnection ()
	{
		super();
	}
	/*
	 * @see net.community.chest.net.NetConnection#connect(java.lang.String, int)
	 */
	@Override
	public void connect (String host, int port) throws IOException
	{
		connect(host, port, null);
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolNetConnection#connect(java.lang.String, net.community.chest.net.proto.text.NetServerWelcomeLine)
	 */
	@Override
	public void connect (String host, NetServerWelcomeLine wl) throws IOException
	{
		connect(host, getDefaultPort(), wl);
	}

	public abstract TextNetConnection getTextNetConnection ();
	/*
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen ()
	{
		final TextNetConnection	conn=getTextNetConnection();
		return (conn != null) && conn.isOpen();
	}
	/*
	 * @see net.community.chest.net.NetConnection#getRemoteHostName()
	 */
	@Override
	public String getRemoteHostName ()
	{
		final TextNetConnection	conn=getTextNetConnection();
		return (conn != null) ? conn.getRemoteHostName() : null;
	}
	/*
	 * @see net.community.chest.net.NetConnection#getRemoteAddress()
	 */
	@Override
	public String getRemoteAddress ()
	{
		final TextNetConnection	conn=getTextNetConnection();
		return (conn != null) ? conn.getRemoteAddress() : null;
	}
	/*
	 * @see net.community.chest.net.NetConnection#getRemotePort()
	 */
	@Override
	public int getRemotePort ()
	{
		final TextNetConnection	conn=getTextNetConnection();
		return (conn != null) ? conn.getRemotePort() : 0;
	}
	/*
	 * @see net.community.chest.net.NetConnection#getLocalPort()
	 */
	@Override
	public int getLocalPort ()
	{
		final TextNetConnection	conn=getTextNetConnection();
		return (conn != null) ? conn.getLocalPort() : 0;
	}
	/*
	 * @see java.io.Flushable#flush()
	 */
	@Override
	public void flush () throws IOException
	{
		final TextNetConnection	conn=getTextNetConnection();
		if (null == conn)
			throw new EOFException("No connection to flush");
		conn.flush();
	}
}
