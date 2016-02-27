package net.community.chest.net.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

import net.community.chest.io.OptionallyCloseable;
import net.community.chest.net.BinaryNetConnection;
import net.community.chest.net.NetConnectionEmbedder;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Embeds a {@link BinaryNetConnection} as an {@link InputStream}
 * 
 * @author Lyor G.
 * @since Jul 4, 2007 8:01:38 AM
 */
public class NetBinaryInputStream extends InputStream
				implements OptionallyCloseable, NetConnectionEmbedder<BinaryNetConnection> {
	/*
	 * @see net.community.chest.io.OptionallyCloseable#isMutableRealClosure()
	 */
	@Override
	public boolean isMutableRealClosure ()
	{
		return true;
	}

	private boolean	_realClosure;
	/*
	 * @see net.community.chest.io.OptionallyCloseable#isRealClosure()
	 */
	@Override
	public boolean isRealClosure ()
	{
		return _realClosure;
	}
	/*
	 * @see net.community.chest.io.OptionallyCloseable#setRealClosure(boolean)
	 */
	@Override
	public void setRealClosure (boolean enabled) throws UnsupportedOperationException
	{
		_realClosure = enabled;
	}

	private BinaryNetConnection	_conn	/* =null */;
 	/*
 	 * @see net.community.chest.net.NetConnectionEmbedder#getConnection()
 	 */
 	@Override
	public BinaryNetConnection getConnection ()
 	{
 		return _conn;
 	}
 	/*
 	 * @see net.community.chest.net.NetConnectionEmbedder#setConnection(net.community.chest.net.NetConnection)
 	 */
 	@Override
	public void setConnection (BinaryNetConnection conn)
 	{
 		_conn = conn;
 	}
 	/**
 	 * @param conn {@link BinaryNetConnection} to "mask" as an {@link InputStream}
 	 * @param autoClose TRUE if {@link #close()} call should also close the
 	 * underlying {@link BinaryNetConnection} (if any)
 	 * @throws IOException if no initial connection provided to mask
 	 * @see #setConnection(BinaryNetConnection)
 	 * @see #setRealClosure(boolean)
 	 */
 	public NetBinaryInputStream (BinaryNetConnection conn, boolean autoClose) throws IOException
	{
		if (null == (_conn=conn))
			throw new IOException("No " + BinaryNetConnection.class.getName() + " instance to mask as " + getClass().getName());

		_realClosure = autoClose;
	}
 	/**
 	 * @param conn {@link BinaryNetConnection} to "mask" as an {@link InputStream}
 	 * <B>Note:</B> automatically closes the the connection when {@link #close()}
 	 * is called on the stream (unless {@link #setRealClosure(boolean)} called
 	 * previous to that)
 	 * @throws IOException if no initial connection provided to mask
 	 */
 	public NetBinaryInputStream (BinaryNetConnection conn) throws IOException
 	{
 		this(conn, true);
 	}
	/*
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException
	{
		final BinaryNetConnection	conn=getConnection();
		if (null == conn)
			throw new EOFException("No connection to read from");

		return conn.read();
	}
	/*
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available () throws IOException
	{
		final BinaryNetConnection	conn=getConnection();
		if (null == conn)
			throw new EOFException("No connection to get available bytes");

		return conn.available();
	}
	/*
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public synchronized void mark (int pos)
	{
		throw new UnsupportedOperationException("Marking N/A on text connection");
	}
	/*
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported ()
	{
		return false;
	}
	/*
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read (byte[] buf, int startPos, int len) throws IOException
	{
		final BinaryNetConnection	conn=getConnection();
		if (null == conn)
			throw new EOFException("No connection to read buffer from");

		return conn.readBytes(buf, startPos, len);
	}
	/*
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read (byte[] buf) throws IOException
	{
		return read(buf, 0, (null == buf) ? 0 : buf.length);
	}
	/*
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public synchronized void reset () throws IOException
	{
		final BinaryNetConnection	conn=getConnection();
		if (null == conn)
			throw new EOFException("No connection to reset");
		else
			throw new StreamCorruptedException("Underlying connection does not support resetting");
	}
	/*
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip (long skipLen) throws IOException
	{
		final BinaryNetConnection	conn=getConnection();
		if (null == conn)
			throw new EOFException("No connection to skip bytes");

		return conn.skip(skipLen);
	}
	/*
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close () throws IOException
	{
		try
		{
			final BinaryNetConnection	conn=getConnection();
			if (isRealClosure() && (conn != null))
				conn.close();
		}
		finally
		{
			setConnection(null);
		}
	}
	/**
	 * Masks a {@link BinaryNetConnection} as an input stream
	 * @param netStream binary net connection
	 * @param autoClose if TRUE then closing the stream also closes the underlying connection
	 * @return stream object
	 * @throws IOException if errors initializing the object (e.g., null net connection)
	 */
	public static final InputStream asInputStream (BinaryNetConnection netStream, boolean autoClose) throws IOException
	{
		return new NetBinaryInputStream(netStream, autoClose);
	}
}
