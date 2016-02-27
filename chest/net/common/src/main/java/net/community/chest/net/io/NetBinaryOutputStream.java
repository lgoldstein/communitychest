package net.community.chest.net.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

import net.community.chest.io.OptionallyCloseable;
import net.community.chest.net.BinaryNetConnection;
import net.community.chest.net.NetConnectionEmbedder;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Embeds a {@link BinaryNetConnection} as an {@link OutputStream}
 * 
 * @author Lyor G.
 * @since Jul 4, 2007 8:23:34 AM
 */
public class NetBinaryOutputStream extends OutputStream
					implements OptionallyCloseable, NetConnectionEmbedder<BinaryNetConnection>{
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
 	 * @param conn {@link BinaryNetConnection} to "mask" as an {@link OutputStream}
 	 * @param autoClose TRUE if {@link #close()} call should also close the
 	 * underlying {@link BinaryNetConnection} (if any)
 	 * @throws IOException if no initial connection provided to mask
 	 * @see #setConnection(BinaryNetConnection)
 	 * @see #setRealClosure(boolean)
 	 */
	public NetBinaryOutputStream (BinaryNetConnection conn, boolean autoClose) throws IOException
	{
		if (null == (_conn=conn))
			throw new IOException("No connection to mask as output stream");

		_realClosure = autoClose;
	}
 	/**
 	 * @param conn {@link BinaryNetConnection} to "mask" as an {@link OutputStream}
 	 * <B>Note:</B> automatically closes the the connection when {@link #close()}
 	 * is called on the stream (unless {@link #setRealClosure(boolean)} called
 	 * previous to that)
 	 * @throws IOException if no initial connection provided to mask
 	 */
	public NetBinaryOutputStream (BinaryNetConnection conn) throws IOException
	{
		this(conn, true);
	}
	/*
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write (int val) throws IOException
	{
		final BinaryNetConnection	conn=getConnection();
		if (null == conn)
			throw new EOFException("No connection to write to");

		final int	nWritten=conn.write(val);
		if (nWritten!= 1)
			throw new StreamCorruptedException("Written " + nWritten + " bytes/characters instead of exactly 1");
	}
	/*
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush () throws IOException
	{
		final BinaryNetConnection	conn=getConnection();
		if (null == conn)
			throw new EOFException("No connection to flush");

		conn.flush();
	}
	/*
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write (byte[] buf, int offset, int len) throws IOException
	{
		final BinaryNetConnection	conn=getConnection();
		if (null == conn)
			throw new EOFException("No connection to write buffer to");

		final int	written=conn.writeBytes(buf, offset, len);
		if (written != len)
			throw new StreamCorruptedException("Conn. output stream write mismatch (" + written + " <> " + len + ")");
	}
	/*
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write (byte[] buf) throws IOException
	{
		write(buf, 0, (null == buf) ? 0 : buf.length);
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
	 * Masks a {@link BinaryNetConnection} as an output stream
	 * @param netStream binary net connection
	 * @param autoClose if TRUE then closing the stream also closes the underlying connection
	 * @return stream object
	 * @throws IOException if errors initializing the stream or no initial connection
	 */
	public static final OutputStream asOutputStream (BinaryNetConnection netStream, boolean autoClose) throws IOException
	{
		return new NetBinaryOutputStream(netStream, autoClose);
	}
}
